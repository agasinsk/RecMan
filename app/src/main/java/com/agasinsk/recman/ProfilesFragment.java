package com.agasinsk.recman;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ProfilesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View fragmentView;

    private ListView profilesListView;
    private ProfileListAdapter mAdapter;

    public ProfilesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfilesFragment newInstance() {
        ProfilesFragment fragment = new ProfilesFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_profiles, container, false);

        // Setup DELETE_PROFILE button
        Button deleteProfileButton = fragmentView.findViewById(R.id.deleteButton);
        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        // Setup USE_PROFILE button
        Button saveProfileButton = fragmentView.findViewById(R.id.useProfileButton);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onProfileSelected(1);
            }
        });

        // Setup the list view
        profilesListView = fragmentView.findViewById(R.id.profilesListView);
        ArrayList<Profile> profilesFromDb = new ArrayList<>();
        profilesFromDb.add(new Profile("source1", "take all", "MP3", true));
        profilesFromDb.add(new Profile("source2", "take newest", "FLAC"));
        profilesFromDb.add(new Profile("source3", "take first", "AIFF"));


        mAdapter = new ProfileListAdapter(getContext(), profilesFromDb);
        profilesListView.setAdapter(mAdapter);

        return fragmentView;
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
        void onProfileSelected(int profileId);
    }
}
