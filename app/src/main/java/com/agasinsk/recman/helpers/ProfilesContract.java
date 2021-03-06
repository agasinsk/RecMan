package com.agasinsk.recman.helpers;

import android.provider.BaseColumns;

final class ProfilesContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ProfilesContract() {}

    /* Inner class that defines the table contents */
    public static class Profile implements BaseColumns {
        public static final String TABLE_NAME = "profiles";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_AUDIO_FORMAT = "audioFormat";
        public static final String COLUMN_NAME_AUDIO_DETAILS = "audioDetails";
        public static final String COLUMN_NAME_SOURCE_FOLDER = "sourceFolder";
        public static final String COLUMN_NAME_FILE_HANDLING = "fileHandling";
        public static final String COLUMN_NAME_IS_DEFAULT = "isDefault";
    }
}