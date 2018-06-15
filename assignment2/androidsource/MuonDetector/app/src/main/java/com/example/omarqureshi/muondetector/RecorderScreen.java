package com.example.omarqureshi.muondetector;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;



public class RecorderScreen extends AppCompatActivity {
    private TextView countDownLabel;
    private Button countDownButton;
    private Button clearButton;
    private CountDownTimer countDownTimer;
    private long timeRemaining = 60000;
    private boolean timerIsRunning;
    private Processor processor = new Processor();
    private TextView eventText;
    private TextView dateText;
    private TextView summaryText;
    private TextView averageText;
    private TextView durationText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }


    public void startStop(){
        if (timerIsRunning){
            stopTimer();
        } else {
            startTimer();
        }
    }


    public void generateEvent(){
        Random rand = new Random();
        int randomInt = rand.nextInt(4) + 1;
        if (randomInt==1){
            processor.addEvent();
            String newEventString = Integer.toString(processor.getEventCount());
            eventText.setText(newEventString);
            eventText.setVisibility(View.VISIBLE);
           DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
           String strDate = df.format(processor.getCurrentTime());
           String fullStamp = "Last Event Stamp: "+strDate;
           dateText.setText(fullStamp);
           dateText.setVisibility(View.VISIBLE);
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
                generateEvent();
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


    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

