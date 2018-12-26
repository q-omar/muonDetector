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
	private boolean isRecording = false;
	private static final int MAX_EVENTS = 1000; // might need this later

	private ArrayList<MuonEvent> eventData = new ArrayList<>();

	//add these two variables to a potential subject class
	private List<Observer> observersList = new ArrayList<Observer>();//subject class holds a list of all observers watching it
	private int state; //arbitrary variable that may change during the course of this class

    private String location;     // User's current location; might want to store this elsewhere later?

    public Processor(Context context) {
        usb = new FtdiDeviceAdaptor(context);
    }

    /**
     * Attempts to connect to the detector.
     * @return true if connection was successful, false if not
     */
    public boolean tryConnection() {
        String[] connectedDevices = usb.getDeviceNames();
        if (connectedDevices.length > 0) {
            usb.initializeConnection(connectedDevices[0]);
        }
        return usb.getFTDIConnected();
    }

    /**
	 * Gets the number of events that have occurred since recording began.
	 */
	public int getEventCount() {
		return eventData.size();
	}

    /**
     * Edits the user's current location
     */
    public void setLocation(String loc) {
        location = loc;
    }

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

		double result = (double)(eventData.size())/timeDifference(startTime, endTime);
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

	public int getUpdatedEventCount() {
		updateEvents();
		return getEventCount();
	}

	/**
	 * Deletes all recorded event data.
	 */
	public void clearEvents() {
		eventData = new ArrayList<>();
	}

	public void updateEvents() {
		ArrayList<String> rawData = usb.readDeviceBuffer();
		String currentTime = getCurrentTime().toString();
		String strCurrentTime = currentTime.toString();
		for (String entry:rawData) {
			eventData.add(new MuonEvent(eventData.size() + 1, location, currentTime, strCurrentTime + entry));
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

	public ArrayList<MuonEvent> getEventData() {
		ArrayList<MuonEvent> returnList = new ArrayList<>();
		for (MuonEvent entry:eventData) {
			returnList.add(new MuonEvent(entry));
		}
		return returnList;
	}
	public ArrayList<String> getStrEventData() {
		ArrayList<String> returnList = new ArrayList<>();
		for (MuonEvent entry:eventData) {
			returnList.add(entry.toString());
		}
		return returnList;
	}
	public static ArrayList<MuonEvent> parseStrEventToMuonEvent(ArrayList<String> stringEventArrayList) {
		ArrayList<MuonEvent> returnArrayList = new ArrayList<>();
		for (String entry:stringEventArrayList) {
			returnArrayList.add(MuonEvent.toMuonEvent(entry));
		}
		return returnArrayList;
	}

}


	













