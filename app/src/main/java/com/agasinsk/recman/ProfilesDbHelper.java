package com.agasinsk.recman;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProfilesDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "RecManProfiles.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProfilesContract.Profiles.TABLE_NAME + " (" +
                    ProfilesContract.Profiles._ID + " INTEGER PRIMARY KEY," +
                    ProfilesContract.Profiles.COLUMN_NAME_NAME + " TEXT," +
                    ProfilesContract.Profiles.COLUMN_NAME_AUDIO_FORMAT + " TEXT," +
                    ProfilesContract.Profiles.COLUMN_NAME_AUDIO_DETAILS + " TEXT," +
                    ProfilesContract.Profiles.COLUMN_NAME_SOURCE_FOLDER + " TEXT," +
                    ProfilesContract.Profiles.COLUMN_NAME_FILE_HANDLING + " TEXT," +
                    ProfilesContract.Profiles.COLUMN_NAME_IS_DEFAULT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProfilesContract.Profiles.TABLE_NAME;

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