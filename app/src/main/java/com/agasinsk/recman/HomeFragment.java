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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.agasinsk.recman.helpers.FileUtils;
import com.agasinsk.recman.helpers.FilesHandler;
import com.agasinsk.recman.helpers.ProfilesRepository;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.agasinsk.recman.ConversionJobService.RESULT_CONVERSION_FAILED;
import static com.agasinsk.recman.ConversionJobService.RESULT_CONVERSION_OK;
import static com.agasinsk.recman.UploadJobService.RESULT_UPLOAD_FAILED;
import static com.agasinsk.recman.UploadJobService.RESULT_UPLOAD_OK;

public class HomeFragment extends Fragment {
    private final String RECMAN_TAG = "RecMan:HomeFragment";
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
    private View mTotalProgressTextView;
    private TextView mFileCountTextView;
    private FloatingActionButton mFab;
    private ListView mFileListView;

    private ArrayAdapter<CharSequence> mFileHandlingAdapter;
    private ArrayAdapter<CharSequence> mAudioFormatAdapter;

    private FileDtoListAdapter mFileListAdapter;
    private int mProgressFraction;

    public HomeFragment() {
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

    private void setProfilesRepository(ProfilesRepository mProfilesRepository) {
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
        mFilesHandler = new FilesHandler();
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

        mProgressBar = fragmentView.findViewById(R.id.homeProgressBar);
        mTotalProgressTextView = fragmentView.findViewById(R.id.totalProgressTextView);
        mFileCountTextView = fragmentView.findViewById(R.id.fileCountTextView);

        // Setup convertFAB
        mFab = fragmentView.findViewById(R.id.goFab);
        mFab.setOnClickListener(view -> {
            if (mListener.checkIfUserIsAuthenticated()) {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                convertAudioFiles(selectedFolderPath, fileHandling, audioFormat);
            } else {
                Toast.makeText(getContext(), R.string.toast_user_not_found_error, Toast.LENGTH_SHORT).show();
                mListener.onSilentAuthenticationFailed();
            }
        });

        mFileListView = fragmentView.findViewById(R.id.filesListView);
        mFileListAdapter = new FileDtoListAdapter(getContext(), R.layout.file_list_item, new ArrayList<>());
        mFileListView.setAdapter(mFileListAdapter);

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
                Log.d(RECMAN_TAG, "Uri: " + selectedFolderUri.toString());
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

        int fileToConvertCount = 0;
        try {
            File[] files = directory.listFiles(File::isFile);
            filesToConvert = mFilesHandler.getFilesWithHandling(files, fileHandling, audioFormat);
            fileToConvertCount = filesToConvert.size();
            if (fileToConvertCount == 0) {
                Log.e(RECMAN_TAG, "No files were found to be converted!");
                Toast.makeText(getContext(), "No files were found to be converted!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(RECMAN_TAG, "An error occurred while selecting files!", e);
        }

        ArrayList<FileDto> fileDtos = new ArrayList<>(fileToConvertCount);
        for (int i = 0; i < fileToConvertCount; i++) {
            File fileToConvert = filesToConvert.get(i);
            int fileId = i + 1;
            fileDtos.add(new FileDto(fileId, fileToConvert.getName()));
            mListener.setUpConversionIntent(fileToConvert.getPath(), audioFormat, fileId, fileToConvertCount);
        }

        mProgressFraction = (int)Math.ceil(100 / (2 * (double)fileToConvertCount));

        mFab.setEnabled(false);
        mTotalProgressTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mFileListView.setVisibility(View.VISIBLE);
        mFileCountTextView.setVisibility(View.VISIBLE);
        String fileCountString = getString(R.string.files_count, 0, fileToConvertCount);
        mFileCountTextView.setText(fileCountString);

        String descriptionText = getString(R.string.conversion_description, fileToConvertCount);
        Toast.makeText(getContext(), descriptionText, Toast.LENGTH_LONG).show();

        mFileListAdapter.clear();
        mFileListAdapter.addAll(fileDtos);
        mFileListAdapter.notifyDataSetChanged();
    }

    public void showProgressUI(int resultCode, int fileId, int totalFileCount) {
        FileDto fileDto = mFileListAdapter.getItem(fileId);
        int currentProgress = mProgressBar.getProgress();

        String fileCountString = getString(R.string.files_count, fileId, totalFileCount);
        mFileCountTextView.setText(fileCountString);
        switch (resultCode) {
            case RESULT_CONVERSION_OK:
                fileDto.progress = 50;
                mProgressBar.setProgress(currentProgress + mProgressFraction);
                break;
            case RESULT_CONVERSION_FAILED:
                fileDto.hasError = true;
                break;
            case RESULT_UPLOAD_OK:
                fileDto.progress = 100;
                mProgressBar.setProgress(currentProgress + mProgressFraction);
                if (fileId == totalFileCount) {
                    String toastText = getString(R.string.finish_description, totalFileCount);
                    Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
                    mFab.setEnabled(true);
                }
                break;
            case RESULT_UPLOAD_FAILED:
                fileDto.hasError = true;
                break;
        }
        mFileListAdapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        void askForProfileName();

        void onSilentAuthenticationFailed();

        boolean checkIfUserIsAuthenticated();

        void setUpConversionIntent(String path, String audioFormat, int fileId, int totalFileCount);
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
