package com.agasinsk.recman;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class ProfilesFragment extends Fragment {

    private final String LOG_TAG = "ProfilesFragment";

    private OnFragmentInteractionListener mListener;
    private View fragmentView;

    private ListView profilesListView;
    private ProfileListAdapter mAdapter;
    private Profile selectedProfile;

    public ProfilesFragment() {
        // Required empty public constructor
    }

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
                if(selectedProfile != null) {
                    Log.i(LOG_TAG, "About to delete profile with id " + selectedProfile.id);
                }
            }
        });

        // Setup USE_PROFILE button
        Button setDefaultButton = fragmentView.findViewById(R.id.setDefaultButton);
        setDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedProfile != null) {
                    Log.i(LOG_TAG, "About to set profile with id " + selectedProfile.id + " as default");
                }
            }
        });

        // Setup USE_PROFILE button
        Button useProfileButton = fragmentView.findViewById(R.id.useProfileButton);
        useProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedProfile != null) {
                    Log.i(LOG_TAG, "About to use profile with id " + selectedProfile.id);
                    mListener.onProfileSelected(selectedProfile.id);
                }
            }
        });

        // Setup the list view
        profilesListView = fragmentView.findViewById(R.id.profilesListView);
        ArrayList<Profile> profilesFromDb = new ArrayList<>();

        //TODO: get profiles from db
        profilesFromDb.add(new Profile(1,"source1", "take all", "MP3", true));
        profilesFromDb.add(new Profile(2,"source2", "take newest", "FLAC"));
        profilesFromDb.add(new Profile(3,"source3", "take first", "AIFF"));
        profilesFromDb.add(new Profile(4,"sourceLongerThanOtherSources", "take me out", "AIFF"));

        mAdapter = new ProfileListAdapter(getContext(),R.layout.profile_list_item, profilesFromDb);
        profilesListView.setAdapter(mAdapter);

        profilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Log.i(LOG_TAG, "Profile with id " + position + " selected");
                selectedProfile = (Profile)parent.getItemAtPosition(position);
            }
        });

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
