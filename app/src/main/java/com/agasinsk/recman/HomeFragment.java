package com.agasinsk.recman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.agasinsk.recman.helpers.FileUtils;
import com.agasinsk.recman.helpers.FilesHandler;
import com.agasinsk.recman.helpers.ProfilesRepository;
import com.agasinsk.recman.microsoft.graph.GraphServiceController;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.DriveItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class HomeFragment extends Fragment {
    private final String RECMAN_TAG = "RecMan:Home";
    private final int SOURCE_FOLDER_REQUEST_CODE = 100;

    private String selectedFolderPath = "";
    private TextView selectedFolderTextView;

    private Profile defaultProfile;
    private Profile mProfileToSave;

    private ProfilesRepository mProfilesRepository;
    private FilesHandler mFilesHandler;
    private OnFragmentInteractionListener mListener;

    private View fragmentView;
    private ProgressBar mProgressBar;
    private ArrayAdapter<CharSequence> mFileHandlingAdapter;
    private ArrayAdapter<CharSequence> mAudioFormatAdapter;

    private GraphServiceController mGraphServiceController;

    public HomeFragment() {
        mFilesHandler = new FilesHandler();
        mGraphServiceController = new GraphServiceController(getContext());
    }

    public static HomeFragment newInstance(ProfilesRepository profilesRepository) {
        return newInstance(null, profilesRepository);
    }

    public static HomeFragment newInstance(Profile defaultProfile, ProfilesRepository profilesRepository) {
        HomeFragment fragment = new HomeFragment();
        fragment.setDefaultProfile(defaultProfile);
        fragment.setProfilesRepository(profilesRepository);
        return fragment;
    }

    public void setProfilesRepository(ProfilesRepository mProfilesRepository) {
        if (mProfilesRepository == null) {
            this.mProfilesRepository = new ProfilesRepository(getActivity().getApplicationContext());
        }
        this.mProfilesRepository = mProfilesRepository;
    }

    private void setDefaultProfile(Profile defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup source folder selection
        selectedFolderTextView = fragmentView.findViewById(R.id.selectedFolder);
        final Button selectFolderButton = fragmentView.findViewById(R.id.folderButton);
        selectFolderButton.setOnClickListener(v -> performFolderSearch());

        /* Setup spinner for audio formats */
        final Spinner audioFormatSpinner = fragmentView.findViewById(R.id.audioFormatSpinner);
        mAudioFormatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audio_formats, android.R.layout.simple_spinner_item);

        mAudioFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioFormatSpinner.setAdapter(mAudioFormatAdapter);

        /* Setup spinner for file handling */
        final Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
        mFileHandlingAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.file_handlings, android.R.layout.simple_spinner_item);
        mFileHandlingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileHandlingSpinner.setAdapter(mFileHandlingAdapter);

        // Setup SAVE_PROFILE button
        Button saveProfileButton = fragmentView.findViewById(R.id.saveProfileButton);
        saveProfileButton.setOnClickListener(v -> {
            if (selectedFolderPath.equals("")) {
                Toast.makeText(getContext(), "Select source folder first!", Toast.LENGTH_SHORT).show();
            } else {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();

                mProfileToSave = new Profile(selectedFolderPath, fileHandling, audioFormat);
                mListener.askForProfileName();
            }
        });

        // Setup convertFAB
        FloatingActionButton fab = fragmentView.findViewById(R.id.goFab);
        fab.setOnClickListener(view -> {
            if (mListener.checkIfUserIsAuthenticated()) {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                convertAudioFiles(selectedFolderPath, fileHandling, audioFormat);
            } else {
                Toast.makeText(getContext(), R.string.toast_user_not_found_error, Toast.LENGTH_SHORT).show();
                mListener.onSilentAuthenticationFailed();
            }
        });

        mProgressBar = fragmentView.findViewById(R.id.homeProgressBar);
        new GetDefaultProfileTask().execute(defaultProfile != null);

        return fragmentView;
    }

    private void performFolderSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, SOURCE_FOLDER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == SOURCE_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri selectedFolderUri = resultData.getData();
                Log.i(RECMAN_TAG, "Uri: " + selectedFolderUri.toString());
                selectedFolderPath = FileUtils.getFullPathFromTreeUri(selectedFolderUri, getActivity());

                selectedFolderTextView.setText(selectedFolderPath);
            }
        }
    }

    public void saveProfileWithName(String profileName) {
        if (mProfileToSave != null) {
            mProfileToSave.setName(profileName);
            new SaveProfileTask().execute(mProfileToSave);
        }
    }

    private void convertAudioFiles(String folderPath, String fileHandling, String audioFormat) {
        File directory = new File(folderPath);
        List<File> filesToConvert = new ArrayList<>();

        try {
            File[] files = directory.listFiles(File::isFile);
            filesToConvert = mFilesHandler.getFilesWithHandling(files, fileHandling, audioFormat);
            if (filesToConvert.size() == 0) {
                Log.e(RECMAN_TAG, "No files were found to be converted!");
                Toast.makeText(getContext(), "No files were found to be converted!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(RECMAN_TAG, "An error occurred while selecting files!", e);
        }

        for (File file : filesToConvert) {
            mProgressBar.setVisibility(View.VISIBLE);
            AndroidAudioConverter.with(getContext())
                    .setFile(file)
                    .setFormat(AudioFormat.valueOf(audioFormat))
                    .setCallback(new IConvertCallback() {
                        @Override
                        public void onSuccess(File convertedFile) {
                            Log.i(RECMAN_TAG, "File " + convertedFile.getName() + " successfully converted!");
                            uploadConvertedFileToOneDrive(convertedFile);
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e(RECMAN_TAG, "An error occurred during file conversion!", error);
                            Toast.makeText(getContext(), R.string.toast_conversion_error, Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    })
                    .convert();
            Toast.makeText(getContext(), R.string.toast_conversion_started, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadConvertedFileToOneDrive(File convertedFile) {
        try {
            mGraphServiceController.uploadFileToOneDrive(convertedFile, new ICallback<DriveItem>() {
                @Override
                public void success(DriveItem driveItem) {
                    Log.i(RECMAN_TAG, "Successfully uploaded file " + driveItem.name + " to OneDrive");
                    Toast.makeText(getContext(), "Successfully uploaded file " + driveItem.name + " to OneDrive", Toast.LENGTH_LONG).show();
                    removeFile(driveItem.name);
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void failure(ClientException ex) {
                    Log.e(RECMAN_TAG, "Exception on file upload to OneDrive ", ex);
                }
            });
        } catch (Exception ex) {
            Log.e(RECMAN_TAG, "Exception on file upload " + ex.getLocalizedMessage());
        }
    }

    private void removeFile(String name) {

        File fileToDelete = new File(selectedFolderPath, name);
        if (fileToDelete.isFile() && fileToDelete.canRead()) {
            if (!fileToDelete.delete()) {
                Log.e(RECMAN_TAG, "An error occurred while deleting file " + name);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void askForProfileName();

        void onSilentAuthenticationFailed();

        boolean checkIfUserIsAuthenticated();
    }

    private class GetDefaultProfileTask extends AsyncTask<Boolean, Void, Profile> {
        protected Profile doInBackground(Boolean... params) {
            Boolean profileWasPassed = params[0];
            if (profileWasPassed) {
                return defaultProfile;
            }
            return mProfilesRepository.getDefaultProfile();
        }

        protected void onProgressUpdate(Void... params) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.homeProgressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(Profile result) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.homeProgressBar);
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                defaultProfile = result;
                Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
                fileHandlingSpinner.setSelection(mFileHandlingAdapter.getPosition(defaultProfile.getFileHandling()), true);

                Spinner audioFormatSpinner = fragmentView.findViewById(R.id.audioFormatSpinner);
                audioFormatSpinner.setSelection(mAudioFormatAdapter.getPosition(defaultProfile.getAudioFormat()), true);

                selectedFolderTextView.setText(defaultProfile.getSourceFolder());
                selectedFolderPath = defaultProfile.getSourceFolder();
            }
        }
    }

    private class SaveProfileTask extends AsyncTask<Profile, Void, Long> {
        protected Long doInBackground(Profile... profiles) {
            Profile profileToSave = profiles[0];
            return mProfilesRepository.saveProfile(profileToSave);
        }

        protected void onProgressUpdate(Void... params) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.homeProgressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(Long result) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.homeProgressBar);
            progressBar.setVisibility(View.INVISIBLE);

            Log.i(RECMAN_TAG, "Profile saved with id: " + result);
            if (result > 0) {
                Toast.makeText(getContext(), R.string.toast_profile_created, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.toast_database_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
