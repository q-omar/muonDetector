package com.example.omarqureshi.muondetector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;


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
    private EditText locationInput;

    private SharedPreferences sharedPrefs;

    // conversion factors for milliseconds to hours/minutes/seconds
    private final long MILLISECONDS_PER_HOUR = 60*60000;
    private final long MILLISECONDS_PER_MINUTE = 60000;
    private final long MILLISECONDS_PER_SECOND = 1000;

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

        locationInput = findViewById(R.id.locationInput);

        // Change user's location after they enter info
        locationInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    updateLocation();
                }
            }
        });
        locationInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateLocation();
                    return true;
                }
                return false;
            }
        });

        boolean isConnected = processor.tryConnection();

        connectionText = (TextView) findViewById(R.id.connectionLabelID);

        if (isConnected) {
            connectionText.setText("Connected");
            connectionText.setVisibility(View.VISIBLE);
        } else {
            connectionText.setText("Not Connected");
            connectionText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Changes the user's current location after they finish editing it.
     */
    private void updateLocation() {
        Editable location = locationInput.getText();  // Get entered text
        String locString;
        if (location == null) {     // If user hasn't entered a location, use "Unset"
            locString = "Unset";
        } else {
            locString = location.toString(); // Else use what they entered
        }

        Log.d("myTag", locString);      // Debug output location text
        processor.setLocation(locString);
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

    /**
     * Switches the timer on/off when the user presses the record button.
     */
    public void startStop(){
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

    public void startTimer(){
        logButton.setText("Summary");
        processor.clearEvents();
        processor.switchRecording();
        eventText.setVisibility(View.INVISIBLE);
        dateText.setVisibility(View.INVISIBLE);
        summaryText.setVisibility(View.INVISIBLE);
        durationText.setVisibility(View.INVISIBLE);
        averageText.setVisibility(View.INVISIBLE);

        locationInput.setEnabled(false);        // Prevent user from editing location while recording
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
        countDownButton.setText("Start");
        locationInput.setEnabled(true);        // Allow user to edit location again
        timerIsRunning = false;
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
}

