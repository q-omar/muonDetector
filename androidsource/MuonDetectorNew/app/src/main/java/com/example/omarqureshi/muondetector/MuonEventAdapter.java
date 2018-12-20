package com.example.omarqureshi.muondetector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This class takes MuonEvent data stored in an array and fills the log screen with information.
 */
public class MuonEventAdapter extends ArrayAdapter<MuonEvent> {

    Context context;
    int layoutResourceId;

    // Constructor requires a context and an arraylist of muon events to print to the log
    public MuonEventAdapter(Context context, ArrayList<MuonEvent> data) {
        super(context, 0, data);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MuonEvent muonEvent = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_row, parent, false);
        }
        // Lookup view for each piece of data of the event
        TextView eventNumber = (TextView) convertView.findViewById(R.id.eventNumber);
        TextView eventLocation = (TextView) convertView.findViewById(R.id.eventLocation);
        TextView eventTime = (TextView) convertView.findViewById(R.id.eventTime);

        // Populate the data into the template view using the data object
        eventNumber.setText(muonEvent.getNumberAsString());
        eventLocation.setText(muonEvent.getLocation());
        eventTime.setText(muonEvent.getTimestamp());
        // Return the completed view to render on screen
        return convertView;
    }
    }
