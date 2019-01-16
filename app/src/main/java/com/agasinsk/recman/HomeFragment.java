package com.agasinsk.recman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import cafe.adriel.androidaudioconverter.model.AudioFormat;


public class HomeFragment extends Fragment {


    private final String RECMAN_TAG = "RecMan";

    private final int SOURCE_FOLDER_REQUEST_CODE = 100;
    private String selectedFolderPath = "";
    private Uri selectedFolderUri = null;

    private OnFragmentInteractionListener mListener;
    private View fragmentView;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup source folder selection
        final Button selectFolderButton = fragmentView.findViewById(R.id.folderButton);
        selectFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        /* Setup spinner for audio formats */
        final Spinner spinner = fragmentView.findViewById(R.id.audioFormatSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audio_formats, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        /* Setup spinner for file handling */
        final Spinner fileHandlingSpinner = fragmentView.findViewById(R.id.fileHandlingSpinner);
        ArrayAdapter<CharSequence> fileHandlingsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.file_handlings, android.R.layout.simple_spinner_item);
        fileHandlingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fileHandlingSpinner.setAdapter(fileHandlingsAdapter);

        // Setup spinner for destination
        final Spinner destinationSpinner = fragmentView.findViewById(R.id.destinationSpinner);
        ArrayAdapter<CharSequence> destinationsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.destinations, android.R.layout.simple_spinner_item);
        destinationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        destinationSpinner.setAdapter(destinationsAdapter);

        // Setup SAVE_PROFILE button
        Button saveProfileButton = fragmentView.findViewById(R.id.saveProfileButton);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFolderPath.equals("")) {
                    Toast.makeText(getContext(), "Select source folder first!", Toast.LENGTH_SHORT).show();
                }
                else {
                    String fileHandling = fileHandlingSpinner.getSelectedItem().toString();
                    String audioFormat = spinner.getSelectedItem().toString();
                    String destination = destinationSpinner.getSelectedItem().toString();
                    mListener.onSaveProfile(selectedFolderPath, fileHandling, audioFormat);
                }
            }
        });

        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onSaveProfile(uri);
        }
    }

    public void performFileSearch() {
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

                TextView selectedFolder = fragmentView.findViewById(R.id.selectedFolder);
                selectedFolder.setText(selectedFolderPath);
            }
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onSaveProfile(String sourceFolder, String fileHandling, String audioFormat);
    }
}
