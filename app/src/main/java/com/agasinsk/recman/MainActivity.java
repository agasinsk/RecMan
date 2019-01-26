package com.agasinsk.recman;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.agasinsk.recman.helpers.BundleConstants;
import com.agasinsk.recman.helpers.FileUtils;
import com.agasinsk.recman.helpers.ProfilesRepository;
import com.agasinsk.recman.microsoft.graph.AuthenticationManager;
import com.agasinsk.recman.microsoft.graph.MicrosoftAuthenticationCallback;
import com.agasinsk.recman.models.Profile;
import com.agasinsk.recman.service.ConversionJobService;
import com.agasinsk.recman.service.ServiceResultReceiver;
import com.agasinsk.recman.service.UploadJobService;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import java.util.List;

import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_FAILED;
import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_OK;
import static com.agasinsk.recman.service.ConversionJobService.RESULT_CONVERSION_STARTED;
import static com.agasinsk.recman.service.UploadJobService.RESULT_UPLOAD_FAILED;
import static com.agasinsk.recman.service.UploadJobService.RESULT_UPLOAD_OK;
import static com.agasinsk.recman.HomeFragment.SOURCE_FOLDER_REQUEST_CODE;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        ProfilesFragment.OnFragmentInteractionListener,
        ProfileNameDialogFragment.ProfileNameDialogListener,
        AccountFragment.OnFragmentInteractionListener,
        MicrosoftAuthenticationCallback,
        ServiceResultReceiver.Receiver {

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

    private ServiceResultReceiver mServiceResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProfilesRepository = new ProfilesRepository(getApplicationContext());

        mBottomNavigation = findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment(HomeFragment.newInstance(mProfilesRepository));
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);

        mServiceResultReceiver = new ServiceResultReceiver(new Handler());
        mServiceResultReceiver.setReceiver(this);

        checkForPermissions();
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
                onSilentAuthenticationFailed();
            }
        } catch (Exception e) {
            onSilentAuthenticationFailed();
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

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                mHomeFragment = HomeFragment.newInstance(mProfilesRepository);
                loadFragment(mHomeFragment);
                return true;
            case R.id.navigation_profiles:
                mProfilesFragment = ProfilesFragment.newInstance(mProfilesRepository);
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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "WRITE Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "WRITE Permission denied");
                }
                break;
            }
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "READ Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "READ Permission denied");
                }
                break;
            }
            case PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "INTERNET Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "INTERNET Permission denied");
                }
                break;
            }
            case PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(RECMAN_TAG, "ACCESS_NETWORK_STATE Permission granted");
                } else {
                    Log.e(RECMAN_TAG, "ACCESS_NETWORK_STATE Permission denied");
                }
                break;
            }
        }
    }

    /**
     * Handles redirect response from https://login.microsoftonline.com/common and
     * notifies the MSAL library that the user has completed the authentication
     * dialog
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == SOURCE_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri selectedFolderUri = resultData.getData();
                Log.d(RECMAN_TAG, "Uri: " + selectedFolderUri.toString());
                String selectedFolderPath = FileUtils.getFullPathFromTreeUri(selectedFolderUri, this);

                mHomeFragment.onFolderSelected(selectedFolderPath);
                return;
            }
        }

        PublicClientApplication publicClient = AuthenticationManager
                .getInstance(getApplicationContext())
                .getPublicClient();

        if (publicClient != null) {
            publicClient.handleInteractiveRequestRedirect(requestCode, resultCode, resultData);
        }
    }

    @Override
    public void performFolderSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, SOURCE_FOLDER_REQUEST_CODE);
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

    @Override
    public void setUpConversionIntent(String filePath, String audioFormat, String audioDetails, int fileId, int totalFileCount) {
        Intent conversionIntent = new Intent(this, ConversionJobService.class);
        conversionIntent.putExtra(BundleConstants.AUDIO_FORMAT, audioFormat);
        conversionIntent.putExtra(BundleConstants.AUDIO_DETAILS, audioDetails);
        conversionIntent.putExtra(BundleConstants.RECEIVER, mServiceResultReceiver);
        conversionIntent.putExtra(BundleConstants.FILE_PATH, filePath);
        conversionIntent.putExtra(BundleConstants.FILE_ID, fileId);
        conversionIntent.putExtra(BundleConstants.FILE_TOTAL_COUNT, totalFileCount);
        ConversionJobService.enqueueWork(this, conversionIntent);
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
        mHomeFragment = HomeFragment.newInstance(profile, mProfilesRepository);
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
        isUserAuthenticated = true;
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);
        mHomeFragment = HomeFragment.newInstance(mProfilesRepository);
        loadFragment(mHomeFragment);
    }

    @Override
    public boolean isUserAuthenticated() {
        return isUserAuthenticated;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOutMenuItem:
                if (isUserAuthenticated) {
                    AuthenticationManager.getInstance(getApplicationContext()).disconnect();
                    Toast.makeText(this, R.string.toast_sign_out, Toast.LENGTH_SHORT).show();
                    isUserAuthenticated = false;
                    mBottomNavigation.setSelectedItemId(R.id.navigation_account);
                    mAccountFragment = AccountFragment.newInstance();
                    loadFragment(mAccountFragment);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case RESULT_CONVERSION_STARTED:
                showProgressUI(resultCode, resultData);
                break;
            case RESULT_CONVERSION_OK:
                String filePath = resultData.getString(BundleConstants.FILE_PATH);
                int fileId = resultData.getInt(BundleConstants.FILE_ID);
                int fileCount = resultData.getInt(BundleConstants.FILE_TOTAL_COUNT);
                enqueueFileUploadIntent(filePath, fileId, fileCount);
                mHomeFragment.onOperationCompleted(resultCode, fileId, fileCount);
                break;
            case RESULT_CONVERSION_FAILED:
                showProgressUI(resultCode, resultData);
                break;
            case RESULT_UPLOAD_OK:
                showProgressUI(resultCode, resultData);
                break;
            case RESULT_UPLOAD_FAILED:
                showProgressUI(resultCode, resultData);
                break;
        }
    }

    private void enqueueFileUploadIntent(String filePath, int fileId, int fileCount) {
        Intent fileUploadIntent = new Intent(this, UploadJobService.class);
        fileUploadIntent.putExtra(BundleConstants.FILE_PATH, filePath);
        fileUploadIntent.putExtra(BundleConstants.RECEIVER, mServiceResultReceiver);
        fileUploadIntent.putExtra(BundleConstants.FILE_ID, fileId);
        fileUploadIntent.putExtra(BundleConstants.FILE_TOTAL_COUNT, fileCount);
        UploadJobService.enqueueWork(this, fileUploadIntent);
    }

    private void showProgressUI(int resultCode, Bundle resultData) {
        int fileId = resultData.getInt(BundleConstants.FILE_ID);
        int fileCount = resultData.getInt(BundleConstants.FILE_TOTAL_COUNT);
        mHomeFragment.onOperationCompleted(resultCode, fileId, fileCount);
    }
}
