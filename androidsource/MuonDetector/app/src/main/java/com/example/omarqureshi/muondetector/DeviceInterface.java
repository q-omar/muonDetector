package com.example.omarqureshi.muondetector;

import android.content.Context;

import java.util.ArrayList;

public interface DeviceInterface {
    /**
     *  Meant to initialize the adaptor with the Android context, so it may do its thing. Most adaptors should need this.
     * @param context An instance of Context from the local app's main activity.
     */
    public void initializeContext(Context context);

    /**
     *  Meant to attain the names of all the devices which may be reached by the given adaptor, if avaliable.
     * @return A String[] with all the names of the relevant devices.
     */
    public String[] getDeviceNames();

    /**
     * Meant to initialize the connection to the relevant device, as chosen by the user.
     * @param deviceName The String name or identifier by which the adaptor can identify which one to connect to.
     */
    public void initializeConnection(String deviceName);

    /**
     *  Meant to initialize the connection to the first detected device, without user input.
     */
    public void initializeConnection();

    /**
     * Meant to order the device to copy the data from the device's buffer to the local buffer, if necessary.
     * @return Boolean a True/False on if anything was read.
     */
    public Boolean readDeviceBuffer();

    /**
     * Meant to attain all of the data in the local data buffer of the adaptor.
     * @return An ArrayList<String> of all the data stored in the local data buffer.
     */
    public ArrayList<String> getLocalBuffer();

    /**
     *  Meant to reset both the local and device data buffers for a fresh reading.
     */
    public void purgeBuffers();

}
