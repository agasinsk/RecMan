package com.agasinsk.recman;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.agasinsk.recman.helpers.ProfilesContract;
import com.agasinsk.recman.helpers.ProfilesDbHelper;
import com.agasinsk.recman.helpers.ProfilesRepository;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        ProfilesFragment.OnFragmentInteractionListener,
        ProfileNameDialogFragment.ProfileNameDialogListener {
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;
    private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 201;
    private final int PERMISSIONS_REQUEST_INTERNET = 202;
    private final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 203;
    private final String RECMAN_TAG = "REC:Main";
    private Profile profileToSave;
    private ProfilesRepository mProfilesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProfilesRepository = new ProfilesRepository(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment(HomeFragment.newInstance());

        // Permissions check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForPermissions();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                loadFragment(HomeFragment.newInstance());
                return true;
            case R.id.navigation_profiles:
                loadFragment(ProfilesFragment.newInstance());
                return true;
            case R.id.navigation_account:
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

    @Override
    public Profile getDefaultProfile() {
        return mProfilesRepository.getDefaultProfile();
    }

    @Override
    public void onSaveProfile(String sourceFolder, String fileHandling, String audioFormat) {
        profileToSave = new Profile(sourceFolder, fileHandling, audioFormat);
        showNoticeDialog();
    }

    public void showNoticeDialog() {
        DialogFragment dialog = new ProfileNameDialogFragment();
        dialog.show(getSupportFragmentManager(), "profileNameDialog");
    }

    @Override
    public void onProfileSelected(int profileId) {
        Log.d(RECMAN_TAG, "Profile selected with id: " + profileId);
    }

    @Override
    protected void onDestroy() {
        mProfilesRepository.close();
        super.onDestroy();
    }

    @Override
    public void onProfileNameDialogClosed(String profileName) {
        if (profileToSave != null) {
            profileToSave.setName(profileName);
            saveProfile(profileToSave);
            profileToSave = null;
        }
    }

    public void saveProfile(Profile profileToSave) {
        Log.i(RECMAN_TAG, "Profile is saving!");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = mProfilesRepository.saveProfile(profileToSave);

        Log.i(RECMAN_TAG, "Profile saved with id: " + newRowId);
        if (newRowId > 0) {
            Toast.makeText(this, "Profile successfully saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "An error occurred while saving the profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
