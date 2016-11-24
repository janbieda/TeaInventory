package com.example.android.teainventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.teainventory.data.TeaContract;
import com.example.android.teainventory.data.TeaContract.TeaEntry;

import org.w3c.dom.Text;

/**
 * {@link TeaCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of tea data as its data source. This adapter knows
 * how to create list items for each row of tea data in the {@link Cursor}.
 */
public class TeaCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link TeaCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public TeaCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the tea data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current tea can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final int teaId = cursor.getInt(cursor.getColumnIndexOrThrow(TeaEntry._ID));
        final int teaQty = cursor.getInt(cursor.getColumnIndex(TeaEntry.COLUMN_TEA_QUANTITY));
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of tea attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(TeaContract.TeaEntry.COLUMN_TEA_NAME);
        int priceColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(TeaEntry.COLUMN_TEA_QUANTITY);

        // Read the tea attributes from the Cursor for the current tea
        String teaName = cursor.getString(nameColumnIndex);
        String teaPrice = cursor.getString(priceColumnIndex);
        final String teaQuantity = cursor.getString(quantityColumnIndex);

        // Update the TextViews with the attributes for the current tea
        nameTextView.setText(teaName);
        summaryTextView.setText("Price: " + teaPrice + " GBP");
        quantityTextView.setText("Currently in stock: " + teaQuantity);

        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                if (teaQty > 0) {
                    int mItemQty;
                    mItemQty = (teaQty - 1);
                    values.put(TeaEntry.COLUMN_TEA_QUANTITY, mItemQty);
                    Uri uri = ContentUris.withAppendedId(TeaEntry.CONTENT_URI, teaId);
                    context.getContentResolver().update(uri, values, null, null);
                }
                context.getContentResolver().notifyChange(TeaEntry.CONTENT_URI, null);
            }
        });
    }
}
