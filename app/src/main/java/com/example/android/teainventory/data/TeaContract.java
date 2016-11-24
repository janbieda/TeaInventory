package com.example.android.teainventory.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Tea app.
 */
public final class TeaContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private TeaContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.teainventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_TEA = "teas";

    /**
     * Inner class that defines constant values for the tea database table.
     * Each entry in the table represents a single tea.
     */
    public static final class TeaEntry implements BaseColumns {

        /** The content URI to access tea data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEA);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of teas.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEA;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single tea.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEA;

        /** Name of database table for teas */
        public final static String TABLE_NAME = "teas";

        /**
         * Unique ID number for the tea (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the tea.
         *
         * Type: TEXT
         */
        public final static String COLUMN_TEA_NAME ="name";

        /**
         * Type of the tea.
         *
         * The only possible values are {@link #TYPE_BLACK}, {@link #TYPE_GREEN},
         * or {@link #TYPE_HERBAL}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_TEA_TYPE = "type";

        /**
         * Price of the tea.
         *
         * Type: REAL
         */
        public final static String COLUMN_TEA_PRICE = "price";

        /**
         * Quantity of the tea.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_TEA_QUANTITY = "quantity";

        /**
         * Image of the tea.
         *
         * Type: TEXT
         */
        public final static String COLUMN_TEA_IMAGE = "image";

        /**
         * Possible values for the type of the tea.
         */
        public static final int TYPE_BLACK = 0;
        public static final int TYPE_GREEN = 1;
        public static final int TYPE_HERBAL = 2;

        /**
         * Returns whether or not the given type is {@link #TYPE_BLACK}, {@link #TYPE_GREEN},
         * or {@link #TYPE_HERBAL}.
         */
        public static boolean isValidType(int type) {
            if (type == TYPE_BLACK || type == TYPE_GREEN || type == TYPE_HERBAL) {
                return true;
            }
            return false;
        }
    }

}

