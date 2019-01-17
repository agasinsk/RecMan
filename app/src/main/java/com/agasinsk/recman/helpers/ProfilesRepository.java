package com.agasinsk.recman.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.agasinsk.recman.Profile;

import java.util.ArrayList;

public class ProfilesRepository {
    // TODO: do this in a task
    protected ProfilesDbHelper mDbHelper;

    public ProfilesRepository(Context context) {
        mDbHelper = new ProfilesDbHelper(context);
    }

    public Profile getDefaultProfile() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = ProfilesContract.Profile._ID + " ASC LIMIT 1";

        Cursor cursor = db.query(
                ProfilesContract.Profile.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        // take any profile
        if (cursor.getCount() < 1) {
            cursor = db.query(
                    ProfilesContract.Profile.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }

        Profile profile = null;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProfilesContract.Profile._ID));
            String profileName = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_NAME));
            String sourceFolder = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_SOURCE_FOLDER));
            String fileHandling = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_FILE_HANDLING));
            String audioFormat = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_AUDIO_FORMAT));
            boolean isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT)) > 0;

            profile = new Profile(id, profileName, sourceFolder, fileHandling, audioFormat, isDefault);
        }
        cursor.close();

        return profile;
    }

    public ArrayList<Profile> getAllProfiles() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ProfilesContract.Profile.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        ArrayList<Profile> dbProfiles = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProfilesContract.Profile._ID));
            String profileName = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_NAME));
            String sourceFolder = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_SOURCE_FOLDER));
            String fileHandling = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_FILE_HANDLING));
            String audioFormat = cursor.getString(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_AUDIO_FORMAT));
            boolean isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT)) > 0;

            Profile profile = new Profile(id, profileName, sourceFolder, fileHandling, audioFormat, isDefault);
            dbProfiles.add(profile);
        }
        cursor.close();

        return dbProfiles;
    }

    public int setProfileAsDefault(int profileId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT, true);

        String selection = ProfilesContract.Profile._ID + " = ?";
        String[] selectionArgs = {String.valueOf(profileId)};

        // set row as default
        int rowsSetAsDefaultCount = db.update(
                ProfilesContract.Profile.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // set other rows as not default
        ContentValues falseValues = new ContentValues();
        falseValues.put(ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT, false);

        selection = ProfilesContract.Profile._ID + " != ?";

        db.update(ProfilesContract.Profile.TABLE_NAME,
                falseValues,
                selection,
                selectionArgs);

        return rowsSetAsDefaultCount;
    }

    public int deleteProfile(int profileId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = ProfilesContract.Profile._ID + " = ?";
        String[] selectionArgs = {String.valueOf(profileId)};

        return db.delete(ProfilesContract.Profile.TABLE_NAME, selection, selectionArgs);

    }

    public long saveProfile(Profile profileToSave) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProfilesContract.Profile.COLUMN_NAME_NAME, profileToSave.getName());
        values.put(ProfilesContract.Profile.COLUMN_NAME_SOURCE_FOLDER, profileToSave.getSourceFolder());
        values.put(ProfilesContract.Profile.COLUMN_NAME_FILE_HANDLING, profileToSave.getFileHandling());
        values.put(ProfilesContract.Profile.COLUMN_NAME_AUDIO_FORMAT, profileToSave.getAudioFormat());
        values.put(ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT, false);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(ProfilesContract.Profile.TABLE_NAME, null, values);
    }

    public void close() {
        mDbHelper.close();
    }
}
