package com.example.omarqureshi.muondetector;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.io.IOException;


public class RecorderScreen extends AppCompatActivity implements Observer{
    private TextView countDownLabel;
    private Button countDownButton;
    private Button logButton;
    private Button settingsBtn;
    private CountDownTimer countDownTimer;
    private long timeRemaining;
    private boolean timerIsRunning;
    public Processor processor;
    private TextView eventText;
    private TextView dateText;
    private TextView summaryText;
    private TextView averageText;
    private TextView durationText;
    private TextView connectionText;
    private TextView eventsMinText;

    private SharedPreferences sharedPrefs;

    // conversion factors for milliseconds to hours/minutes/seconds
    private final long MILLISECONDS_PER_HOUR = 60*60000;
    private final long MILLISECONDS_PER_MINUTE = 60000;
    private final long MILLISECONDS_PER_SECOND = 1000;

    //I took this from an external source, not my code, need to rewrite.
    public static final String TAG = "RecorderScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processor = new Processor(this);
        processor.addObserver(this); //add this line to an observer class that wants to add itself to the observer list watching a subject

        setContentView(R.layout.activity_recorder_screen);
        setTitle("Muon Event Detector");

        countDownLabel = findViewById(R.id.timerLabelID);
        // Load previous settings
        resetTimer();
        countDownLabel.setText(getTimeRemainingAsString());

        countDownButton = findViewById(R.id.recorderButtonID);
        countDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });

        logButton = findViewById(R.id.logButtonID);
        logButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){openLogScreen();
            }
        });

        settingsBtn = findViewById(R.id.settingsButtonID);
        settingsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!timerIsRunning) {
                    showSettingsDialog();
                }
            }
        });

        eventText = findViewById(R.id.eventsLabelID2);
        dateText =  findViewById(R.id.datestampID);
        summaryText = findViewById(R.id.summaryLabelID);
        averageText = findViewById(R.id.averageLabelID);
        durationText = findViewById(R.id.durationLabelID);
        eventsMinText = findViewById(R.id.eventsMinID2);

        boolean isConnected = processor.isConnected();
        if (!isConnected) {
            isConnected = processor.tryConnection();
        }
        connectionText = (TextView) findViewById(R.id.connectionLabelID);

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
     * Resets the timer display using stored preferences.
     */
    private void resetTimer() {
        sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        // Read the stored # of hours
        int hours = sharedPrefs.getInt(getString(R.string.hours_key), 0);

        // Read the stored number of minutes; default is 1
        int minutes = sharedPrefs.getInt(getString(R.string.minutes_key), 1);

        // Read stored # of seconds
        int seconds = sharedPrefs.getInt(getString(R.string.seconds_key), 0);

        // Calculate time remaining in milliseconds
        timeRemaining = ((long)hours) * MILLISECONDS_PER_HOUR;
        timeRemaining += ((long) minutes) * MILLISECONDS_PER_MINUTE;
        timeRemaining += ((long) seconds) * MILLISECONDS_PER_SECOND;

        updateTimer();
    }

    /**
     * Opens a dialog for the user to adjust settings such as timer length.
     */
    private void showSettingsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SettingsDialog settings = SettingsDialog.newInstance("Settings");
        // Update this screen when settings are changed
        settings.setSettingsDialogListener(new SettingsDialog.SettingsDialogListener() {
            @Override
            public void settingsSaved() {
                resetTimer();
            }
        });

        settings.show(fm, "settings_menu");
    }

    /**
     * Opens the log screen upon a button click.
     */
    public void openLogScreen(){

        Intent intent = new Intent(this,LogScreen.class);
        intent.putStringArrayListExtra("MuonData", processor.getStrEventData());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void startStop(){
        updateConnection();
        if (timerIsRunning){
            stopTimer();
        } else {
            startTimer();
        }
    }


    public void updateScreen() {
        String newEventString = Integer.toString(processor.getUpdatedEventCount());

        eventText.setText(newEventString);
        eventText.setVisibility(View.VISIBLE);

        // Update events
        String newEventsMin = Double.toString(processor.getEventsPerMin());
        eventsMinText.setText(newEventsMin);
        eventsMinText.setVisibility(View.VISIBLE);

    }

    public void updateConnection() {
        boolean isConnected = processor.isConnected();

        if (!isConnected) {
            isConnected = processor.tryConnection();
        }
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

    public void startTimer(){
        logButton.setText("Summary");
        processor.clearEvents();
        processor.switchRecording();
        eventText.setVisibility(View.INVISIBLE);
        dateText.setVisibility(View.INVISIBLE);
        summaryText.setVisibility(View.INVISIBLE);
        durationText.setVisibility(View.INVISIBLE);
        averageText.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(timeRemaining, 10) {
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
        resetTimer();
        countDownTimer.cancel();
        updateConnection();
        timerIsRunning = false;
        try{
            processor.exportCSV();
        }
        catch (IOException ex) {
            System.out.println("Error in exporting CSV");
        }
        processor.switchRecording();
    }

    /**
     * Uses the current time remaining in milliseconds and formats it to a string in
     * hours:minutes:seconds format.
     * @return  the time remaining as a String
     */
    private String getTimeRemainingAsString() {
        // Calculate # of hours and subtract from total time
        long hours = timeRemaining/(MILLISECONDS_PER_HOUR);
        String hoursAsString = String.format("%02d",hours);              // format # of hours string with 2 digits

        long remainder = timeRemaining - hours*MILLISECONDS_PER_HOUR;   // time in milliseconds after subtracting hours

        // Calculate # of minutes and subtract from total time
        long minutes = remainder/(MILLISECONDS_PER_MINUTE);
        String minutesAsString = String.format("%02d",minutes);
        remainder = remainder - minutes*MILLISECONDS_PER_MINUTE;

        // Calculate remaining time in seconds
        long seconds = remainder/MILLISECONDS_PER_SECOND;
        String secondsAsString = String.format("%02d",seconds);

        // Return complete timestamp as string
        return hoursAsString + ":" + minutesAsString + ":" + secondsAsString;
    }

    /**
     * Updates the displayed time remaining on screen.
     */
    public void updateTimer(){
        String timeLeftString = getTimeRemainingAsString();
        countDownLabel.setText(timeLeftString);
    }


    // Not used in current version of app
    public void clearScreen(){
        if (processor.getEventCount()==0){
            summaryText.setVisibility(View.INVISIBLE);
            durationText.setVisibility(View.INVISIBLE);
            averageText.setVisibility(View.INVISIBLE);
            dateText.setVisibility(View.INVISIBLE);
            eventText.setVisibility(View.INVISIBLE);
            logButton.setText("Summary");
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
            logButton.setText("Clear");
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

