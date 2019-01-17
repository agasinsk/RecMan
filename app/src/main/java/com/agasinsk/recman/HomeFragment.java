package com.agasinsk.recman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.agasinsk.recman.helpers.FilesHandler;
import com.agasinsk.recman.helpers.ProfilesRepository;

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
    private Uri selectedFolderUri = null;
    TextView selectedFolderTextView;
    private AudioFormat mAudioFormat;

    public void setDefaultProfile(Profile defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    private Profile defaultProfile;
    private Profile mProfileToSave;

    private ProfilesRepository mProfilesRepository;
    private FilesHandler mFilesHandler;
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private ArrayAdapter<CharSequence> mFileHandlingAdapter;
    private ArrayAdapter<CharSequence> mAudioFormatAdapter;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(Profile defaultProfile) {
        HomeFragment fragment = new HomeFragment();
        fragment.setDefaultProfile(defaultProfile);
        return fragment;
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
        mProfilesRepository = new ProfilesRepository(getActivity().getApplicationContext());
        mFilesHandler = new FilesHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        // Setup spinner for destination
        final Spinner destinationSpinner = fragmentView.findViewById(R.id.destinationSpinner);
        ArrayAdapter<CharSequence> DestinationsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.destinations, android.R.layout.simple_spinner_item);
        DestinationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        destinationSpinner.setAdapter(DestinationsAdapter);

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
            String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
            String audioFormat = audioFormatSpinner.getSelectedItem().toString();
            convertAudioFiles(selectedFolderPath, fileHandling, audioFormat);
        });

        new GetDefaultProfileTask().execute(defaultProfile != null);

        return fragmentView;
    }

    public void performFolderSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, SOURCE_FOLDER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == SOURCE_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                selectedFolderUri = resultData.getData();
                Log.i(RECMAN_TAG, "Uri: " + selectedFolderUri.toString());
                selectedFolderPath = FileUtils.getFullPathFromTreeUri(selectedFolderUri, getActivity());

                selectedFolderTextView.setText(selectedFolderPath);
            }
        }
    }

    public void convertAudioFiles(String folderPath, String fileHandling, String audioFormat) {
        File directory = new File(folderPath);
        File[] filesToConvert = new File[0];

        try {
            File[] files = directory.listFiles(f -> f.isFile());
            filesToConvert = mFilesHandler.getFilesWithHandling(files, fileHandling);
            if (filesToConvert.length == 0) {
                Log.e(RECMAN_TAG, "No files were found to be converted!");
                Toast.makeText(getContext(), "No files were found to be converted!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(RECMAN_TAG, "An error occurred while selecting files!", e);
        }

        mAudioFormat = AudioFormat.valueOf(audioFormat);
        // TODO: do this in a task
        for (File file : filesToConvert) {
            AndroidAudioConverter.with(getContext())
                    .setFile(file)
                    .setFormat(AudioFormat.valueOf(audioFormat))
                    .setCallback(new IConvertCallback() {
                        @Override
                        public void onSuccess(File convertedFile) {
                            Log.i(RECMAN_TAG, "File " + convertedFile.getName() + " successfully converted!");
                            // saveFileToOneDrive(convertedFile);
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e(RECMAN_TAG, "An error occurred during file conversion!", error);
                        }
                    })
                    .convert();
        }
    }

    public void saveProfileWithName(String profileName) {
        if (mProfileToSave != null) {
            mProfileToSave.setName(profileName);
            new SaveProfileTask().execute(mProfileToSave);
        }
    }

    public interface OnFragmentInteractionListener {
        void askForProfileName();
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

    private class ConvertAudioFilesTask extends AsyncTask<File, Void, List<File>> {
        protected List<File> doInBackground(File... filesToConvert) {
            List<File> convertedFiles = new ArrayList<>();
            IConvertCallback callback = new IConvertCallback() {
                @Override
                public void onSuccess(File convertedFile) {
                    Log.i(RECMAN_TAG, "File " + convertedFile.getName() + " successfully converted!");
                    convertedFiles.add(convertedFile);
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e(RECMAN_TAG, "An error occurred during file conversion!", error);
                }
            };

            for (File file : filesToConvert) {
                AndroidAudioConverter.with(getContext())
                        .setFile(file)
                        .setFormat(AudioFormat.MP3)
                        .setCallback(callback)
                        .convert();
            }
            return convertedFiles;
        }

        protected void onProgressUpdate(Void... params) {
            fragmentView.findViewById(R.id.homeProgressBar).setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(List<File> result) {
            fragmentView.findViewById(R.id.homeProgressBar).setVisibility(View.GONE);

            // TODO: upload files to OneDrive
            if (result.size() > 0) {
                Toast.makeText(getContext(), R.string.toast_files_converted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.toast_conversion_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
