package com.example.omarqureshi.muondetector;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class LogScreen extends AppCompatActivity {

    private ListView listView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_screen);

        // Dummy data to show how the log screen will look
        // Will need to send an array list of data via the Processor later?
        Intent thisIntent = getIntent();
        ArrayList<MuonEvent> eventData = Processor.parseStrEventToMuonEvent(thisIntent.getStringArrayListExtra("MuonData"));

        MuonEventAdapter adapter = new MuonEventAdapter(this, eventData);

        listView1 = (ListView)findViewById(R.id.listView1);

        View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row, null);
        listView1.addHeaderView(header);

        listView1.setAdapter(adapter);

    }
}
