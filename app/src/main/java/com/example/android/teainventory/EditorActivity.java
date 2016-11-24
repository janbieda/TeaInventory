package com.example.android.teainventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.teainventory.data.TeaContract.TeaEntry;

import java.io.FileDescriptor;
import java.io.IOError;
import java.io.IOException;

/**
 * Allows user to create a new tea or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 0;

    private static final String STATE_URI = "STATE_URI";
    /**
     * Identifier for the tea data loader
     */
    private static final int EXISTING_TEA_LOADER = 0;
    private ImageView mImageView;
    private FloatingActionButton mFab;
    private Uri mUri;
    /**
     * Content URI for the existing tea (null if it's a new tea)
     */
    private Uri mCurrentTeaUri;

    /**
     * EditText field to enter the tea name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the tea type
     */
    private Spinner mTypeSpinner;

    /**
     * EditText field to enter the tea price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the tea quantity
     */
    private EditText mQuantityEditText;

    private TextView mImageTextView;

    private String imageUri;

    private Button mIncrement;

    private Button mDecrement;

    private Button mOrder;

    private int quantity;

    private String name;


    /**
     * Type of the tea. The possible valid values are in the TeaContract.java file:
     * {@link TeaEntry#TYPE_BLACK}, {@link TeaEntry#TYPE_GREEN}, or
     * {@link TeaEntry#TYPE_HERBAL}.
     */
    private int mType = TeaEntry.TYPE_BLACK;

    /**
     * Boolean flag that keeps track of whether the tea has been edited (true) or not (false)
     */
    private boolean mTeaHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mTeaHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTeaHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new tea or editing an existing one.
        Intent intent = getIntent();
        mCurrentTeaUri = intent.getData();

        // If the intent DOES NOT contain a tea content URI, then we know that we are
        // creating a new tea.
        if (mCurrentTeaUri == null) {
            // This is a new tea, so change the app bar to say "Add a Tea"
            setTitle(getString(R.string.editor_activity_title_new_tea));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a tea that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing tea, so change app bar to say "Edit Tea"
            setTitle(getString(R.string.editor_activity_title_edit_tea));

            // Initialize a loader to read the tea data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_TEA_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_tea_name);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        mPriceEditText = (EditText) findViewById(R.id.edit_tea_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_tea_quantity);
        mDecrement = (Button) findViewById(R.id.button_decrement);
        mIncrement = (Button) findViewById(R.id.button_increment);
        mOrder = (Button) findViewById(R.id.button_order);
        mImageView = (ImageView) findViewById(R.id.image);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity += 1;

                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        mDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity -= 1;

                if (quantity < 0) {
                    quantity = 0;
                }
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        mQuantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    quantity = Integer.parseInt(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    mQuantityEditText.removeTextChangedListener(this);
                    String Value = mQuantityEditText.getText().toString();
                    mQuantityEditText.setText(Value);
                    mQuantityEditText.addTextChangedListener(this);
                }
            }
        });

        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = mNameEditText.getText().toString().trim();
                String type;
                if (mType == 0) {
                    type = "Black";
                } else if (mType == 1) {
                    type = "Green";
                } else {
                    type = "Herbal";
                }

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"teawarehouse@tea.co.uk"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Tea ordering");
                intent.putExtra(Intent.EXTRA_TEXT, "Please order ... boxes of tea: "
                        + name
                        + ", of type: " + type
                        + ". We currently have only "
                        + quantity
                        + " in stock.");

                startActivity(Intent.createChooser(intent, "Send Email"));

            }
        });


        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the tea.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_green))) {
                        mType = TeaEntry.TYPE_GREEN;
                    } else if (selection.equals(getString(R.string.type_herbal))) {
                        mType = TeaEntry.TYPE_HERBAL;
                    } else {
                        mType = TeaEntry.TYPE_BLACK;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = TeaEntry.TYPE_BLACK;
            }
        });
    }

    /**
     * Get user input from editor and save tea into database.
     */
    private void saveTea() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        // Check if this is supposed to be a new tea
        // and check if all the fields in the editor are blank
        if (mCurrentTeaUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) && mType == TeaEntry.TYPE_BLACK) {
            // Since no fields were modified, we can return early without creating a new tea.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, getString(R.string.editor_update_tea_data_missing),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and tea attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(TeaEntry.COLUMN_TEA_NAME, nameString);
        values.put(TeaEntry.COLUMN_TEA_TYPE, mType);
        values.put(TeaEntry.COLUMN_TEA_PRICE, priceString);
        values.put(TeaEntry.COLUMN_TEA_QUANTITY, quantityString);
        values.put(TeaEntry.COLUMN_TEA_IMAGE, imageUri);

        // Determine if this is a new or existing tea by checking if mCurrentTeaUri is null or not
        if (mCurrentTeaUri == null) {
            // This is a NEW tea, so insert a new tea into the provider,
            // returning the content URI for the new tea.
            Uri newUri = getContentResolver().insert(TeaEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_tea_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_tea_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING tea, so update the tea with content URI: mCurrentTeaUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentTeaUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentTeaUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_tea_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_tea_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new tea, hide the "Delete" menu item.
        if (mCurrentTeaUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save tea to database
                saveTea();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the tea hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mTeaHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the tea hasn't changed, continue with handling back button press
        if (!mTeaHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all tea attributes, define a projection that contains
        // all columns from the tea table
        String[] projection = {
                TeaEntry._ID,
                TeaEntry.COLUMN_TEA_NAME,
                TeaEntry.COLUMN_TEA_TYPE,
                TeaEntry.COLUMN_TEA_PRICE,
                TeaEntry.COLUMN_TEA_QUANTITY,
                TeaEntry.COLUMN_TEA_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentTeaUri,         // Query the content URI for the current tea
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of tea attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_NAME);
            int typeColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            try {
                String picture = cursor.getString(imageColumnIndex);
                Uri imageUri = Uri.parse(picture);
                mImageView.setImageBitmap(getBitmapFromUri(imageUri));

            } catch (NullPointerException e) {

            }

            mQuantityEditText.setText(String.valueOf(quantity));

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));


            // Type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Black, 1 is Green, 2 is Herbal).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (type) {
                case TeaEntry.TYPE_GREEN:
                    mTypeSpinner.setSelection(1);
                    break;
                case TeaEntry.TYPE_HERBAL:
                    mTypeSpinner.setSelection(2);
                    break;
                default:
                    mTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mTypeSpinner.setSelection(0);
        mImageView.setImageBitmap(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the tea.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this tea.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the tea.
                deleteTea();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the tea.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the tea in the database.
     */
    private void deleteTea() {
        // Only perform the delete if this is an existing tea.
        if (mCurrentTeaUri != null) {
            // Call the ContentResolver to delete the tea at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentTeaUri
            // content URI already identifies the tea that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTeaUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_tea_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_tea_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            mImageTextView.setText(mUri.toString());
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        // The document selected by the user won't be returned in the intent.
        // Instead, a URI to that document will be contained in the return intent
        // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && resultData != null && resultData.getData() != null) {
            Uri uri = resultData.getData();
            imageUri = uri.toString();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView productPicture = (ImageView) findViewById(R.id.image);
                productPicture.setImageBitmap(bitmap);
                productPicture.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

}