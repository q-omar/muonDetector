package com.example.omarqureshi.muondetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 This class initiates the Recorder
 You can find the About Screen under res/layout/activity_recorder_screen.xml
 */

public class RecorderScreen extends AppCompatActivity implements Observer{

    /**
     * Screen Elements
     */

    private TextView countDownLabel;
    private Button countDownButton;
    private Button clearButton;
    private TextView eventText;
    private TextView dateText;
    private TextView summaryText;
    private TextView averageText;
    private TextView durationText;
    private TextView connectionText;
    private TextView eventsMinText;

    /**
     * Attributes
     */
    private CountDownTimer countDownTimer;
    private long timeRemaining = 60000;
    private boolean timerIsRunning;
    public Processor processor;

    /**
     * Constants
     */

    public static final String TAG = "RecorderScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Needed for interfacing with the detector
        processor = new Processor(this);
        processor.addObserver(this); //add this line to an observer class that wants to add itself to the observer list watching a subject
        boolean isConnected = processor.tryConnection();

        //Layout Elements
        setContentView(R.layout.activity_recorder_screen);
        setTitle("Muon Event Detector");

        countDownLabel = findViewById(R.id.timerLabelID);
        countDownButton = findViewById(R.id.recorderButtonID);
        countDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });
        clearButton = findViewById(R.id.clearButtonID);
        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clearScreen();
            }
        });

        eventText = (TextView) findViewById(R.id.eventsLabelID2);
        dateText = (TextView) findViewById(R.id.datestampID);
        summaryText = (TextView) findViewById(R.id.summaryLabelID);
        averageText = (TextView) findViewById(R.id.averageLabelID);
        durationText = (TextView) findViewById(R.id.durationLabelID);
        eventsMinText = (TextView) findViewById(R.id.eventsMinID2);
        connectionText = (TextView) findViewById(R.id.connectionLabelID);

        //Report if it's connected to the device yet or not.
        if (isConnected) {
            connectionText.setText("Connected");
            connectionText.setVisibility(View.VISIBLE);
        } else {
            connectionText.setText("Not Connected");
            connectionText.setVisibility(View.VISIBLE);
            countDownButton.setText("Try Connecting");
        }

        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();
    }

    /**
     * Method executed by the bottom left button, the one that handles turning on or off the recording.
     */
    public void startStop(){
        boolean isConnected = processor.isConnected();
        updateConnection();
        if (!isConnected) {
            tryConnection();
        }
        else {
            if (timerIsRunning){
                stopTimer();
            } else {
                startTimer();
            }
        }
        try {
            processor.exportCSV();
        }
        catch (IOException ex) {
            //TextView debugText = findViewById(R.id.debugText);
            //debugText.setText(ex.getMessage());
        }
    }


    public void updateScreen() {
        String newEventString = Integer.toString(processor.getEventCount());

        eventText.setText(newEventString);
        eventText.setVisibility(View.VISIBLE);

        // Update events
        String newEventsMin = Double.toString(processor.getEventsPerMin());
        eventsMinText.setText(newEventsMin);
        eventsMinText.setVisibility(View.VISIBLE);

    }

    public void updateConnection() {
        boolean isConnected = processor.isConnected();
        if (isConnected) {
            connectionText.setText("Connected");
            connectionText.setVisibility(View.VISIBLE);
            countDownButton.setText("Connected");
        } else {
            connectionText.setText("Not Connected");
            connectionText.setVisibility(View.VISIBLE);
            countDownButton.setText("Try Connecting");
        }
    }

    /**
     * This function attempts to connect to the detector.
     */
    public void tryConnection() {
        boolean isConnected = processor.isConnected();
        if (!isConnected) {
            processor.tryConnection();
        }
    }

    public void startTimer(){
        clearButton.setText("Summary");
        processor.clearEvents();
        processor.switchRecording();
        eventText.setVisibility(View.INVISIBLE);
        dateText.setVisibility(View.INVISIBLE);
        summaryText.setVisibility(View.INVISIBLE);
        durationText.setVisibility(View.INVISIBLE);
        averageText.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long l ) {
                timeRemaining = l;

                updateScreen();
                updateTimer();
            }

            @Override
            public void onFinish() {
                startStop();
            }
        }.start();
        countDownButton.setText("Recording...");
        timerIsRunning = true;
    }


    public void stopTimer(){
        timeRemaining= 60000;
        countDownTimer.cancel();
        countDownButton.setText("Start");
        timerIsRunning = false;
        String resetString = "1:00";
        countDownLabel.setText(resetString);
        processor.switchRecording();
    }


    public void updateTimer(){
        int seconds = (int) timeRemaining % 60000/1000;
        String timeLeftString;
        timeLeftString = ":" + seconds;
        countDownLabel.setText(timeLeftString);
    }



    public void clearScreen(){
        if (processor.getEventCount()==0){
            summaryText.setVisibility(View.INVISIBLE);
            durationText.setVisibility(View.INVISIBLE);
            averageText.setVisibility(View.INVISIBLE);
            dateText.setVisibility(View.INVISIBLE);
            eventText.setVisibility(View.INVISIBLE);
            clearButton.setText("Summary");
        } else if (!timerIsRunning){

            String averageString = Double.toString(processor.getEventsPerMin());
            processor.clearEvents();
            dateText.setVisibility(View.INVISIBLE);
            String durationString = Double.toString(processor.timeDifference(processor.getStartTime(), processor.getStopTime()));
            summaryText.setVisibility(View.VISIBLE);
            durationText.setVisibility(View.VISIBLE);
            averageText.setVisibility(View.VISIBLE);
            durationText.setText("Total elapsed duration: " + durationString + " m");
            averageText.setText(averageString + " events/minute detected");
            clearButton.setText("Clear");
        }

    }

    //add this method to an observer class
    @Override
    public void update(){ 
       //do something to this class on an update from the subject
       //possibly use processor.getState() or processor.setState() to do something with its state (processor is arbitrary subject in this case)
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    //Below is not our code; came from https://mobikul.com/getting-read-write-permission-external-storage-android/ .
    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }
}

