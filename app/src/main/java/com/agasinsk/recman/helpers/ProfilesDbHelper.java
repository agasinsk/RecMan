package com.agasinsk.recman.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ProfilesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "RecManProfiles.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProfilesContract.Profile.TABLE_NAME + " (" +
                    ProfilesContract.Profile._ID + " INTEGER PRIMARY KEY," +
                    ProfilesContract.Profile.COLUMN_NAME_NAME + " TEXT," +
                    ProfilesContract.Profile.COLUMN_NAME_SOURCE_FOLDER + " TEXT," +
                    ProfilesContract.Profile.COLUMN_NAME_FILE_HANDLING + " TEXT," +
                    ProfilesContract.Profile.COLUMN_NAME_AUDIO_FORMAT + " TEXT," +
                    ProfilesContract.Profile.COLUMN_NAME_AUDIO_DETAILS + " TEXT," +
                    ProfilesContract.Profile.COLUMN_NAME_IS_DEFAULT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProfilesContract.Profile.TABLE_NAME;

    public ProfilesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}