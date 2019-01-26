package com.agasinsk.recman;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.agasinsk.recman.helpers.FilesHandler;
import com.agasinsk.recman.helpers.ProfilesRepository;
import com.agasinsk.recman.models.FileDto;
import com.agasinsk.recman.models.Profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_FAILED;
import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_OK;
import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_STARTED;
import static com.agasinsk.recman.service.UploadJobService.RESULT_UPLOAD_FAILED;
import static com.agasinsk.recman.service.UploadJobService.RESULT_UPLOAD_OK;

public class HomeFragment extends Fragment {
    private final String RECMAN_TAG = "RecMan:HomeFragment";
    static final int SOURCE_FOLDER_REQUEST_CODE = 100;

    private String mSelectedFolderPath = "";
    private TextView mSelectedFolderTextView;

    private Profile defaultProfile;
    private Profile mProfileToSave;
    private int mFilesWithError;

    private ProfilesRepository mProfilesRepository;
    private FilesHandler mFilesHandler;
    private OnFragmentInteractionListener mListener;

    private View fragmentView;
    private ProgressBar mProgressBar;
    private TextView mFileCountTextView;
    private FloatingActionButton mFab;
    private ListView mFileListView;

    private ArrayAdapter<CharSequence> mFileHandlingAdapter;
    private ArrayAdapter<CharSequence> mAudioFormatAdapter;
    private ArrayAdapter<CharSequence> mAudioDetailsAdapter;

    private FileDtoListAdapter mFileListAdapter;
    private int mProgressFraction;
    private Button mClearButton;
    private Spinner mAudioDetailsSpinner;

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
        mSelectedFolderTextView = fragmentView.findViewById(R.id.selectedFolder);
        final Button selectFolderButton = fragmentView.findViewById(R.id.folderButton);
        selectFolderButton.setOnClickListener(v -> mListener.performFolderSearch());

        /* Setup spinner for audio details */
        mAudioDetailsSpinner = fragmentView.findViewById(R.id.audioDetailsSpinner);
        mAudioDetailsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audio_details, android.R.layout.simple_spinner_item);

        mAudioDetailsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAudioDetailsSpinner.setAdapter(mAudioDetailsAdapter);
        mAudioDetailsSpinner.setVisibility(View.GONE);

        /* Setup spinner for audio formats */
        final Spinner audioFormatSpinner = fragmentView.findViewById(R.id.audioFormatSpinner);
        mAudioFormatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audio_formats, android.R.layout.simple_spinner_item);

        mAudioFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioFormatSpinner.setAdapter(mAudioFormatAdapter);

        audioFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                if (audioFormat.equals("MP3")) {
                    mAudioDetailsSpinner.setVisibility(View.VISIBLE);
                } else {
                    mAudioDetailsSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                if (audioFormat.equals("MP3")) {
                    mAudioDetailsSpinner.setVisibility(View.VISIBLE);
                } else {
                    mAudioDetailsSpinner.setVisibility(View.GONE);
                }
            }
        });

        /* Setup spinner for file handling */
        final Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
        mFileHandlingAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.file_handlings, android.R.layout.simple_spinner_item);
        mFileHandlingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileHandlingSpinner.setAdapter(mFileHandlingAdapter);

        // Setup SAVE_PROFILE button
        Button saveProfileButton = fragmentView.findViewById(R.id.saveProfileButton);
        saveProfileButton.setOnClickListener(v -> {
            if (mSelectedFolderPath.equals("")) {
                Toast.makeText(getContext(), "Select source folder first!", Toast.LENGTH_SHORT).show();
            } else {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                String audioDetails = mAudioDetailsSpinner.getSelectedItem().toString();

                mProfileToSave = new Profile(mSelectedFolderPath, fileHandling, audioFormat, audioDetails);
                mListener.askForProfileName();
            }
        });

        // Setup CLEAR button
        mClearButton = fragmentView.findViewById(R.id.clearButton);
        mClearButton.setOnClickListener(v -> {
            resetUI();
        });

        // Setup FAB
        mFab = fragmentView.findViewById(R.id.goFab);
        mFab.setOnClickListener(view -> {
            if (mListener.checkIfUserIsAuthenticated()) {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                String audioDetails = mAudioDetailsSpinner.getSelectedItem().toString();
                convertAudioFiles(mSelectedFolderPath, fileHandling, audioFormat, audioDetails);
            } else {
                Toast.makeText(getContext(), R.string.toast_user_not_found_error, Toast.LENGTH_SHORT).show();
                mListener.onSilentAuthenticationFailed();
            }
        });

        mProgressBar = fragmentView.findViewById(R.id.homeProgressBar);
        mFileCountTextView = fragmentView.findViewById(R.id.fileCountTextView);

        // Setup file list view
        mFileListView = fragmentView.findViewById(R.id.filesListView);
        mFileListAdapter = new FileDtoListAdapter(getContext(), R.layout.file_list_item, new ArrayList<>());
        mFileListView.setAdapter(mFileListAdapter);

        new GetDefaultProfileTask().execute(defaultProfile != null);

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void resetUI() {
        mFab.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setProgress(0);
        mFileListView.setVisibility(View.GONE);
        mFileCountTextView.setVisibility(View.GONE);
        mFileCountTextView.setText("");
        mClearButton.setVisibility(View.GONE);
        mFilesWithError = 0;

        mFileListAdapter.clear();
        mFileListAdapter.notifyDataSetChanged();
    }

    public void onFolderSelected(String selectedFolder) {
        mSelectedFolderPath = selectedFolder;
        mSelectedFolderTextView.setText(mSelectedFolderPath);
    }

    public void saveProfileWithName(String profileName) {
        if (mProfileToSave != null) {
            mProfileToSave.setName(profileName);
            new SaveProfileTask().execute(mProfileToSave);
        }
    }

    private void convertAudioFiles(String folderPath, String fileHandling, String audioFormat, String audioDetails) {
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
                resetUI();
                return;
            }
        } catch (IOException e) {
            Log.e(RECMAN_TAG, "An error occurred while selecting files!", e);
        }

        ArrayList<FileDto> fileDtos = new ArrayList<>(fileToConvertCount);
        for (int i = 0; i < fileToConvertCount; i++) {
            File fileToConvert = filesToConvert.get(i);
            int fileId = i + 1;
            fileDtos.add(new FileDto(fileId, fileToConvert.getName()));
            mListener.setUpConversionIntent(fileToConvert.getPath(), audioFormat, audioDetails, fileId, fileToConvertCount);
        }

        mProgressFraction = (int) Math.ceil(100 / (2 * (double) fileToConvertCount));

        showProgressUI(fileToConvertCount, fileDtos);
    }

    private void showProgressUI(int fileToConvertCount, ArrayList<FileDto> fileDtos) {
        mFab.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
        mFileListView.setVisibility(View.VISIBLE);
        mFileCountTextView.setVisibility(View.VISIBLE);
        String fileCountString = getString(R.string.files_count, 0, fileToConvertCount);
        mFileCountTextView.setText(fileCountString);

        String descriptionText = getString(R.string.conversion_description, fileToConvertCount);
        Toast.makeText(getContext(), descriptionText, Toast.LENGTH_SHORT).show();

        mFileListAdapter.clear();
        mFileListAdapter.addAll(fileDtos);
        mFileListAdapter.notifyDataSetChanged();
    }

    public void onOperationCompleted(int resultCode, int fileId, int totalFileCount) {
        String fileCountString = getString(R.string.files_count, fileId, totalFileCount);
        mFileCountTextView.setText(fileCountString);

        mProgressBar.setIndeterminate(false);
        int currentProgress = mProgressBar.getProgress();
        FileDto fileDto = mFileListAdapter.getItem(fileId);

        switch (resultCode) {
            case RESULT_CONVERSION_STARTED:
                fileDto.setStarted(true);
                break;
            case RESULT_CONVERSION_OK:
                fileDto.setProgress(50);
                mProgressBar.setProgress(currentProgress + mProgressFraction);
                break;
            case RESULT_CONVERSION_FAILED:
                fileDto.setHasError(true);
                mFilesWithError++;
                mProgressBar.setProgress(currentProgress + 2 * mProgressFraction);
                mClearButton.setVisibility(View.VISIBLE);
                break;
            case RESULT_UPLOAD_OK:
                fileDto.setProgress(100);
                mProgressBar.setProgress(currentProgress + mProgressFraction);
                if (fileId == totalFileCount) {
                    mFab.setEnabled(true);
                    mClearButton.setVisibility(View.VISIBLE);
                    String toastText = getString(R.string.finish_description, totalFileCount);
                    Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
                }
                break;
            case RESULT_UPLOAD_FAILED:
                fileDto.setHasError(true);
                mFilesWithError++;
                mClearButton.setVisibility(View.VISIBLE);
                break;
        }

        if (mFilesWithError == totalFileCount) {
            Toast.makeText(getContext(), R.string.toast_conversion_error, Toast.LENGTH_LONG).show();
            mProgressBar.setProgress(100, true);
            mProgressBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        mFileListAdapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        void performFolderSearch();

        void askForProfileName();

        void onSilentAuthenticationFailed();

        boolean checkIfUserIsAuthenticated();

        void setUpConversionIntent(String path, String audioFormat, String audioDetails, int fileId, int totalFileCount);
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
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                defaultProfile = result;
                Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
                fileHandlingSpinner.setSelection(mFileHandlingAdapter.getPosition(defaultProfile.getFileHandling()), true);

                Spinner audioFormatSpinner = fragmentView.findViewById(R.id.audioFormatSpinner);
                audioFormatSpinner.setSelection(mAudioFormatAdapter.getPosition(defaultProfile.getAudioFormat()), true);

                Spinner audioDetailsSpinner = fragmentView.findViewById(R.id.audioDetailsSpinner);
                audioDetailsSpinner.setSelection(mAudioDetailsAdapter.getPosition(defaultProfile.getAudioDetails()), true);

                mSelectedFolderTextView.setText(defaultProfile.getSourceFolder());
                mSelectedFolderPath = defaultProfile.getSourceFolder();
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
