package com.agasinsk.recman;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.agasinsk.recman.helpers.ProfilesRepository;
import com.microsoft.identity.client.PublicClientApplication;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        ProfilesFragment.OnFragmentInteractionListener,
        ProfileNameDialogFragment.ProfileNameDialogListener,
        AccountFragment.OnFragmentInteractionListener {

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;
    private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 201;
    private final int PERMISSIONS_REQUEST_INTERNET = 202;
    private final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 203;
    private final String RECMAN_TAG = "REC:Main";

    private ProfilesRepository mProfilesRepository;
    private ProfilesFragment mProfilesFragment;
    private HomeFragment mHomeFragment;
    private AccountFragment mAccountFragment;
    private BottomNavigationView mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProfilesRepository = new ProfilesRepository(getApplicationContext());

        mBottomNavigation = findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment(HomeFragment.newInstance(null));
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);

        // Permissions check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForPermissions();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                mHomeFragment = HomeFragment.newInstance(null);
                loadFragment(mHomeFragment);
                return true;
            case R.id.navigation_profiles:
                mProfilesFragment = ProfilesFragment.newInstance();
                loadFragment(mProfilesFragment);
                return true;
            case R.id.navigation_account:
                mAccountFragment = AccountFragment.newInstance();
                loadFragment(mAccountFragment);
                return true;
        }
        return false;
    };

    private void checkForPermissions() {
        // Check for INTERNET permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_INTERNET);
        }

        // Check for ACCESS_NETWORK_STATE permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        }

        // Check for READ permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Check for WRITE permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "WRITE Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "WRITE Permission denied");
                }
                return;
            }
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "READ Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "READ Permission denied");
                }
                return;
            }
            case PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "INTERNET Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "INTERNET Permission denied");
                }
                return;
            }
            case PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "ACCESS_NETWORK_STATE Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "ACCESS_NETWORK_STATE Permission denied");
                }
                return;
            }
        }
    }

    /**
     * Handles redirect response from https://login.microsoftonline.com/common and
     * notifies the MSAL library that the user has completed the authentication
     * dialog
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        PublicClientApplication publicClient = AuthenticationManager
                .getInstance(getApplicationContext())
                .getPublicClient();

        if (publicClient != null) {
            publicClient.handleInteractiveRequestRedirect(requestCode, resultCode, data);
        }
    }

    @Override
    public void askForProfileName() {
        DialogFragment dialog = new ProfileNameDialogFragment();
        dialog.show(getSupportFragmentManager(), "profileNameDialog");
    }

    @Override
    public void onProfileSelected(Profile profile) {
        Log.d(RECMAN_TAG, "Profile selected with id: " + profile.getId());
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);
        mHomeFragment = HomeFragment.newInstance(profile);
        loadFragment(mHomeFragment);
    }

    @Override
    protected void onDestroy() {
        mProfilesRepository.close();
        super.onDestroy();
    }

    @Override
    public void onProfileNameDialogClosed(String profileName) {
        mHomeFragment.saveProfileWithName(profileName);
    }

    @Override
    public void onSuccessfulAuthentication(String userName) {
        Log.d(RECMAN_TAG, "Successfully authenticated user " + userName);
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);
        mHomeFragment = HomeFragment.newInstance();
        loadFragment(mHomeFragment);
    }

    @Override
    public void onAuthenticationError() {

    }
}
