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
import android.widget.Toast;

import com.agasinsk.recman.helpers.ProfilesRepository;
import com.agasinsk.recman.microsoft.graph.AuthenticationManager;
import com.agasinsk.recman.microsoft.graph.MicrosoftAuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        ProfilesFragment.OnFragmentInteractionListener,
        ProfileNameDialogFragment.ProfileNameDialogListener,
        AccountFragment.OnFragmentInteractionListener,
        MicrosoftAuthenticationCallback {

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;
    private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 201;
    private final int PERMISSIONS_REQUEST_INTERNET = 202;
    private final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 203;
    private final String RECMAN_TAG = "RecMan:MainActivity";
    private boolean isUserAuthenticated;

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

        tryToConnectToMicrosoftGraphSilently();
    }

    private void tryToConnectToMicrosoftGraphSilently() {
        AuthenticationManager authenticationManager = AuthenticationManager.getInstance(getApplicationContext());
        try {
            List<User> users = authenticationManager.getPublicClient().getUsers();

            if (users != null && users.size() == 1) {
                authenticationManager.callAcquireTokenSilent(users.get(0),
                        true,
                        this);
            } else {
                Log.e(RECMAN_TAG, "No user was found during authentication");
                //TODO: add toast and move to Account fragment
            }
        } catch (Exception e) {
            String errorText = getResources().getString(R.string.title_text_error);
            //showConnectErrorUI(errorText);
        }
    }

    @Override
    public void onMicrosoftAuthenticationSuccess(AuthenticationResult authenticationResult) {
        String userName;
        try {
            userName = authenticationResult.getUser().getName();
            isUserAuthenticated = true;
            Log.d(RECMAN_TAG, "Successfully authenticated user " + userName);
            Toast.makeText(getApplicationContext(), "Successfully authenticated user " + userName, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException npe) {
            Log.e(RECMAN_TAG, npe.getMessage(), npe);
            Toast.makeText(getApplicationContext(), R.string.toast_user_not_found_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMicrosoftAuthenticationError(MsalException exception) {
        Log.e(RECMAN_TAG, "An error occurred during authentication", exception);
        isUserAuthenticated = false;
        Toast.makeText(getApplicationContext(), R.string.toast_connect_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMicrosoftAuthenticationCancel() {
        Log.e(RECMAN_TAG, "Authentication cancelled");
        isUserAuthenticated = false;
        Toast.makeText(getApplicationContext(), "Authentication cancelled", Toast.LENGTH_SHORT).show();
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
    public boolean checkIfUserIsAuthenticated() {
        return isUserAuthenticated;
    }

    public void onSilentAuthenticationFailed() {
        Log.d(RECMAN_TAG, "Silent authentication failed. Opening Account Fragment.");
        mBottomNavigation.setSelectedItemId(R.id.navigation_account);
        mAccountFragment = AccountFragment.newInstance();
        loadFragment(mAccountFragment);
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
    public boolean isUserAuthenticated() {
        return isUserAuthenticated;
    }
}
