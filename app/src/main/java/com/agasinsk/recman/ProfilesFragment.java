package com.agasinsk.recman;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.agasinsk.recman.helpers.ProfilesRepository;

import java.util.ArrayList;
import java.util.List;

public class ProfilesFragment extends Fragment {
    private final String LOG_TAG = "ProfilesFragment";
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private ListView profilesListView;
    private ProfileListAdapter mAdapter;
    private Profile selectedProfile;
    private ProfilesRepository mProfilesRepository;

    public ProfilesFragment() {
        // Required empty public constructor
    }

    public static ProfilesFragment newInstance() {
        return new ProfilesFragment();
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
        mProfilesRepository.close();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mProfilesRepository = new ProfilesRepository(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_profiles, container, false);

        // Setup DELETE_PROFILE button
        Button deleteProfileButton = fragmentView.findViewById(R.id.deleteButton);
        deleteProfileButton.setOnClickListener(v -> {
            if (selectedProfile != null) {
                Log.i(LOG_TAG, "About to delete profile with id " + selectedProfile.getId());
                //mListener.deleteProfile(selectedProfile.id);
                new DeleteProfileTask().execute(selectedProfile.getId());
            }
        });

        // Setup SET_DEFAULT button
        Button setDefaultButton = fragmentView.findViewById(R.id.setDefaultButton);
        setDefaultButton.setOnClickListener(v -> {
            if (selectedProfile != null) {
                Log.d(LOG_TAG, "About to set profile with id " + selectedProfile.getId() + " as default");
                new SetProfileAsDefaultTask().execute(selectedProfile.getId());
            }
        });

        // Setup USE_PROFILE button
        Button useProfileButton = fragmentView.findViewById(R.id.useProfileButton);
        useProfileButton.setOnClickListener(v -> {
            if (selectedProfile != null) {
                Log.d(LOG_TAG, "About to use profile with id " + selectedProfile.getId());
                mListener.onProfileSelected(selectedProfile.getId());
            } else {
                Toast.makeText(getContext(), "You have to select profile to use it!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the list view
        profilesListView = fragmentView.findViewById(R.id.profilesListView);
        mAdapter = new ProfileListAdapter(getContext(), R.layout.profile_list_item, new ArrayList<>());
        profilesListView.setAdapter(mAdapter);
        new GetProfilesTask().execute();

        profilesListView.setOnItemClickListener((parent, view, position, arg3) -> {
            Log.d(LOG_TAG, "Profile with id " + position + " selected");
            selectedProfile = (Profile) parent.getItemAtPosition(position);
        });

        return fragmentView;
    }

    public interface OnFragmentInteractionListener {
        void onProfileSelected(int profileId);
    }

    private class GetProfilesTask extends AsyncTask<Void, Void, List<Profile>> {
        protected List<Profile> doInBackground(Void... profileIds) {
            return mProfilesRepository.getAllProfiles();
        }

        protected void onProgressUpdate(Void... params) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(List<Profile> result) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                mAdapter.clear();
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), R.string.toast_database_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteProfileTask extends AsyncTask<Integer, Void, List<Profile>> {
        protected List<Profile> doInBackground(Integer... profileIds) {
            int profileId = profileIds[0];
            if (mProfilesRepository.deleteProfile(profileId) > 0) {
                return mProfilesRepository.getAllProfiles();
            }

            return null;
        }

        protected void onProgressUpdate(Void... params) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(List<Profile> result) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                Toast.makeText(getContext(), R.string.toast_profile_deleted, Toast.LENGTH_SHORT).show();
                mAdapter.clear();
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), R.string.toast_database_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SetProfileAsDefaultTask extends AsyncTask<Integer, Void, List<Profile>> {
        protected List<Profile> doInBackground(Integer... profileIds) {
            int profileId = profileIds[0];
            if (mProfilesRepository.setProfileAsDefault(profileId) > 0) {
                return mProfilesRepository.getAllProfiles();
            }

            return null;
        }

        protected void onProgressUpdate(Void... params) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(List<Profile> result) {
            ProgressBar progressBar = fragmentView.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                Toast.makeText(getContext(), R.string.toast_profile_set_default, Toast.LENGTH_SHORT).show();
                mAdapter.clear();
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), R.string.toast_database_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
