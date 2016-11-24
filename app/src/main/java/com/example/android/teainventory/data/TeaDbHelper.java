package com.example.android.teainventory.data;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.android.teainventory.data.TeaContract.TeaEntry;

/**
 * Database helper for Tea app. Manages database creation and version management.
 */
public class TeaDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = TeaDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "teas.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link TeaDbHelper}.
     *
     * @param context of the app
     */
    public TeaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the teas table
        String SQL_CREATE_TEAS_TABLE =  "CREATE TABLE " + TeaEntry.TABLE_NAME + " ("
                + TeaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TeaEntry.COLUMN_TEA_NAME + " TEXT NOT NULL, "
                + TeaEntry.COLUMN_TEA_TYPE + " INTEGER NOT NULL, "
                + TeaEntry.COLUMN_TEA_PRICE + " REAL NOT NULL DEFAULT 0, "
                + TeaEntry.COLUMN_TEA_QUANTITY + " INTEGER NOT NULL, "
                + TeaEntry.COLUMN_TEA_IMAGE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_TEAS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}