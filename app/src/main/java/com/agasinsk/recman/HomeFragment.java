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

import java.io.File;
import java.io.IOException;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class HomeFragment extends Fragment {
    private final String RECMAN_TAG = "RecMan:Home";
    private final int SOURCE_FOLDER_REQUEST_CODE = 100;
    private String selectedFolderPath = "";
    private Uri selectedFolderUri = null;
    Profile defaultProfile;
    private FilesHandler mFilesHandler;
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    TextView selectedFolderTextView;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        ArrayAdapter<CharSequence> audioFormatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audio_formats, android.R.layout.simple_spinner_item);

        audioFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioFormatSpinner.setAdapter(audioFormatAdapter);

        /* Setup spinner for file handling */
        final Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
        ArrayAdapter<CharSequence> fileHandlingAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.file_handlings, android.R.layout.simple_spinner_item);
        fileHandlingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileHandlingSpinner.setAdapter(fileHandlingAdapter);

        // Setup spinner for destination
        final Spinner destinationSpinner = fragmentView.findViewById(R.id.destinationSpinner);
        ArrayAdapter<CharSequence> destinationsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.destinations, android.R.layout.simple_spinner_item);
        destinationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        destinationSpinner.setAdapter(destinationsAdapter);

        // Setup SAVE_PROFILE button
        Button saveProfileButton = fragmentView.findViewById(R.id.saveProfileButton);
        saveProfileButton.setOnClickListener(v -> {
            if (selectedFolderPath.equals("")) {
                Toast.makeText(getContext(), "Select source folder first!", Toast.LENGTH_SHORT).show();
            } else {
                String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                String audioFormat = audioFormatSpinner.getSelectedItem().toString();
                mListener.onSaveProfile(selectedFolderPath, fileHandling, audioFormat);
            }
        });

        // Setup convertFAB
        FloatingActionButton fab = fragmentView.findViewById(R.id.goFab);
        fab.setOnClickListener(view -> {
            String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
            String audioFormat = audioFormatSpinner.getSelectedItem().toString();
            convertAudioFiles(selectedFolderPath, fileHandling, audioFormat);
        });

        defaultProfile = mListener.getDefaultProfile();
        if (defaultProfile != null) {
            fileHandlingSpinner.setSelection(fileHandlingAdapter.getPosition(defaultProfile.fileHandling), true);
            fileHandlingSpinner.setSelection(fileHandlingAdapter.getPosition(defaultProfile.fileHandling), true);
            audioFormatSpinner.setSelection(audioFormatAdapter.getPosition(defaultProfile.audioFormat), true);
            selectedFolderTextView.setText(defaultProfile.sourceFolder);
            selectedFolderPath = defaultProfile.sourceFolder;
        }

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

        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Log.i(RECMAN_TAG, "File " + convertedFile.getName() + " successfully converted!");
                // saveFileToOneDrive(convertedFile);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(RECMAN_TAG, "An error occurred during file conversion!", error);
            }
        };

        // TODO: do this in a task
        for (File file : filesToConvert) {
            AndroidAudioConverter.with(getContext())
                    .setFile(file)
                    .setFormat(AudioFormat.valueOf(audioFormat))
                    .setCallback(callback)
                    .convert();
        }
    }

    public interface OnFragmentInteractionListener {
        Profile getDefaultProfile();

        void onSaveProfile(String sourceFolder, String fileHandling, String audioFormat);
    }

   
}
