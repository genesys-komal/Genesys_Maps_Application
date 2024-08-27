package com.example.mapapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mapapplication.originCode.net.models.PlaceAutocompletePrediction;
import com.example.mapapplication.R;

import java.util.ArrayList;

public class AutoCompleteViewAdapter extends ArrayAdapter<PlaceAutocompletePrediction> {
    private LayoutInflater mInflater = null;
    private Activity activity;
    ArrayList<PlaceAutocompletePrediction> list = new ArrayList<>();

    public AutoCompleteViewAdapter(Activity a, ArrayList<PlaceAutocompletePrediction> items) {
        super(a, 0, items);
        activity = a;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = items;
    }

     interface onItemClicked {
         default void onItemsClicked(PlaceAutocompletePrediction placeAutocompletePrediction) {

         }
     }

    public static class ViewHolder {

        public TextView title;
        public TextView description;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_search_suggestion,
                    parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.text_view_fullname);
            holder.description = (TextView) convertView.findViewById(R.id.text_view_name);

            convertView.setTag(holder);
            PlaceAutocompletePrediction placeAutocompletePrediction1 = list.get(position);
            holder.title.setText(placeAutocompletePrediction1.getStructuredFormatting().getMainText());
            holder.description.setText(placeAutocompletePrediction1.getStructuredFormatting().getSecondaryText());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               
            }
        });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

}
