package com.example.omarqureshi.muondetector;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Date;

public class Processor {
	private FtdiDeviceAdaptor usb;

	private Date startTime;
	private Date stopTime;
	private Date currentTime;
	private boolean isRecording = false;
	private int eventCount = 0;		// Number of events over a collection period
	private static final int MAX_EVENTS = 1000; // might need this later

	//add these two variables to a potential subject class
	private List<Observer> observersList = new ArrayList<Observer>();//subject class holds a list of all observers watching it
	private int state; //arbitrary variable that may change during the course of this class 

    public Processor(Context context) {
        usb = new FtdiDeviceAdaptor(context);
    }

    /**
     * Attempts to connect to the detector.
     * @return true if connection was successful, false if not
     */
    public boolean tryConnection() {
        String[] connectedDevices = usb.getDeviceNames();
        String allDevices = Arrays.toString(connectedDevices);
        if (connectedDevices.length > 0) {
            usb.initializeConnection(connectedDevices[0]);
        }
        return usb.getFTDIConnected();
    }

    /**
	 * Gets the number of events that have occurred since recording began.
	 */
	public int getEventCount() {
	    boolean read = usb.readDeviceBuffer();
	    if (read) {
            eventCount = usb.getLocalBuffer().size();
        }
		return eventCount;
	}

	/**
	 * Records a new instance of a muon event.
	 */


	/**
	 * Called when the user chooses to Start/Stop Recording. Saves the appropriate timestamp
	 * for starting or ending recording and toggles the isRecording flag.
	 */
	public void switchRecording() {
		Date timestamp = new Date();
		if (isRecording) {
		    getEventCount();            // Get final number of events
			stopTime = timestamp;		// Stop recording
		} else {
			startTime = timestamp;		// Start recording
			stopTime = null;
			usb.readDeviceBuffer();           // Tell device to begin reading
		}
		isRecording = !isRecording;		// Toggle recording
	}

	/**
	 * Returns when the last data collection period started.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Returns when the last data collection period ended.
	 */
	public Date getStopTime() {
		return stopTime;
	}
	public Date getCurrentTime(){
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * Calculates and returns the number of events per minute of this recording session.
	 */
	public double getEventsPerMin() {
		Date endTime;

		if (isRecording) {
			endTime = new Date();	// If this is called while recording, use current time for calculation
		} else {
			endTime = stopTime;		// Else use the time the last recording ended
		}

		double result = (double)(eventCount)/timeDifference(startTime, endTime);
		result = (double)Math.round(result*1000d)/1000d;
		return result;
	}

	/**
	 * Calculates the temporal difference between two timestamps in minutes.
	 * @param start  the earlier timestamp
	 * @param end  the later timestamp
	 * @return  the difference between the two timestamps in minutes
	 */
	public double timeDifference(Date start, Date end) {
		long difference = end.getTime() - start.getTime();  // Calculates difference in milliseconds
		double diffInSeconds = (double) difference / 1000;
		double diffInMinutes = diffInSeconds / 60;
		diffInMinutes = (double)Math.round(diffInMinutes*1000d)/1000d;
		return diffInMinutes;
	}

	/**
	 * Deletes all recorded event data.
	 */
	public void clearEvents() {
		eventCount = 0;
		if (usb.getFTDIConnected()) {
			usb.purgeBuffers();
		}
	}


	//add these methods to the relevant subject class

	public void setState (int newState){ //set a state and because itll be changed from old state, notify observers
		state = newState; //this method can either be used inside this class via simply setState(4) or 
		notifyObservers(); //outside by a class who has instance of this class via processor.setState(4)
	}

	public int getState(){ //the observing classes can use this getstate() method to check this methods state
		return state;
	}

	public void notifyObservers(){ //triggers the update method in the observer classes which might do a corresponding action
		for (Observer observer : observersList) {
			observer.update();
		}
	}

	public void addObserver(Observer observer){ 
		observersList.add(observer); //a potential observer class can call this method to add itself as an observer 
	}

}


	













