package com.buddhiraj.suitcase;

import android.content.Context;
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
        documentImage.setImageResource(item.getImage());
        documentName.setText(item.getName());
        documentPrice.setText(item.getPrice());
        documentDescription.setText(item.getDescription());

        return rowView;
    }
}
