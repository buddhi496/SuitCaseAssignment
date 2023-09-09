package com.buddhiraj.suitcase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class DocumentAdapter extends ArrayAdapter<DocumentItem> {
    private final Context context;
    private final List<DocumentItem> items;

    public DocumentAdapter(Context context, List<DocumentItem> items) {
        super(context, R.layout.custom_document_item, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_document_item, parent, false);

        ImageView documentImage = rowView.findViewById(R.id.documentImage);
        TextView documentName = rowView.findViewById(R.id.documentName);
        TextView documentPrice = rowView.findViewById(R.id.documentPrice);
        TextView documentDescription = rowView.findViewById(R.id.documentDescription);

        DocumentItem item = items.get(position);

        // Convert the byte array to a Bitmap and set it in the ImageView
        if (item.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(item.getImage(), 0, item.getImage().length);
            documentImage.setImageBitmap(bitmap);
        }

        documentName.setText(item.getName());
        documentPrice.setText(item.getPrice());
        documentDescription.setText(item.getDescription());

        return rowView;
    }
}
