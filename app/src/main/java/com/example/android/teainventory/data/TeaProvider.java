package com.example.android.teainventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.teainventory.data.TeaContract.TeaEntry;

/**
 * {@link ContentProvider} for Tea Inventory app.
 */
public class TeaProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = TeaProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the teas table */
    private static final int TEAS = 100;

    /** URI matcher code for the content URI for a single tea in the teas table */
    private static final int TEA_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.teainventory/teas" will map to the
        // integer code {@link #TEAS}. This URI is used to provide access to MULTIPLE rows
        // of the teas table.
        sUriMatcher.addURI(com.example.android.teainventory.data.TeaContract.CONTENT_AUTHORITY,
                com.example.android.teainventory.data.TeaContract.PATH_TEA, TEAS);

        // The content URI of the form "content://com.example.android.teainventory/teas/#" will map to the
        // integer code {@link #TEA_ID}. This URI is used to provide access to ONE single row
        // of the teas table.
        sUriMatcher.addURI(com.example.android.teainventory.data.TeaContract.CONTENT_AUTHORITY,
                com.example.android.teainventory.data.TeaContract.PATH_TEA + "/#", TEA_ID);
    }

    /** Database helper object */
    private com.example.android.teainventory.data.TeaDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new com.example.android.teainventory.data.TeaDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case TEAS:
                // For the TEAS code, query the teas table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the teas table.
                cursor = database.query(TeaEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TEA_ID:
                // For the TEA_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.teainventory/teas/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = TeaEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the teas table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TeaEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TEAS:
                return insertTea(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a tea into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertTea(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(TeaEntry.COLUMN_TEA_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Tea requires a name");
        }

        // Check that the tea type is valid
        Integer type = values.getAsInteger(TeaEntry.COLUMN_TEA_TYPE);
        if (type == null || !TeaEntry.isValidType(type)) {
            throw new IllegalArgumentException("Tea requires valid type");
        }

        // Check that price is not null
        Float price = values.getAsFloat(TeaEntry.COLUMN_TEA_PRICE);
        if (price == null && price < 0) {
            throw new IllegalArgumentException("Tea requires valid price");
        }

        // Check that quantity is more than 0
        Integer quantity = values.getAsInteger(TeaEntry.COLUMN_TEA_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Tea quantity must be more than 0");
        }

        // Picture input is optional

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new tea with the given values
        long id = database.insert(TeaEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the tea content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TEAS:
                return updateTea(uri, contentValues, selection, selectionArgs);
            case TEA_ID:
                // For the TEA_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TeaEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTea(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update teas in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more teas).
     * Return the number of rows that were successfully updated.
     */
    private int updateTea(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link TeaEntry#COLUMN_TEA_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(TeaEntry.COLUMN_TEA_NAME)) {
            String name = values.getAsString(TeaEntry.COLUMN_TEA_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Tea requires a name");
            }
        }

        // If the {@link TeaEntry#COLUMN_TEA_TYPE} key is present,
        // check that the type value is valid.
        if (values.containsKey(TeaEntry.COLUMN_TEA_TYPE)) {
            Integer type = values.getAsInteger(TeaEntry.COLUMN_TEA_TYPE);
            if (type == null || !TeaEntry.isValidType(type)) {
                throw new IllegalArgumentException("Tea requires valid type");
            }
        }

        // If the {@link TeaEntry#COLUMN_TEA_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(TeaEntry.COLUMN_TEA_PRICE)) {
            // Check that the price is greater than 0
            Float price = values.getAsFloat(TeaEntry.COLUMN_TEA_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Tea requires valid price");
            }
        }

        if (values.containsKey(TeaEntry.COLUMN_TEA_QUANTITY)) {
            // Check that quantity is more or equal to 0
            Integer quantity = values.getAsInteger(TeaEntry.COLUMN_TEA_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Tea quantity must be more than 0");
            }
        }

        // No need to check the picture, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(TeaEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TEAS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TeaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TEA_ID:
                // Delete a single row given by the ID in the URI
                selection = TeaEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(TeaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TEAS:
                return TeaEntry.CONTENT_LIST_TYPE;
            case TEA_ID:
                return TeaEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
