package com.example.omarqureshi.muondetector;

//Todo: Issue: Repeated calls to initialize only break/undo the FT_Device pointer/connection.

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

/**
 * This class mediates the connection and attainment of information from the detector to the rest of the application.
 */
public class UsbDeviceInterface {

    private ArrayList<String> rawData = new ArrayList<>();
    //Associations
    private Context context;                //Needed to initialize the two associations below.
    private UsbManager managerDevice;       //Needed for detecting connected USB devices.
    private FT_Device ftdiDevice;           //Needed for actually processing the incoming USB serial data.


    /**
     * Constructor: Takes in a Context for association only; that's all that is needed.
     * @param context
     */
    public UsbDeviceInterface(Context context) {
        setContextAndManager(context);
    }

    /*
    Getters and Setters.
     */

    /**
     * This method initializes the Context association and the UsbManager association.
     * @param context An instance of Context from Android.
     */
    public void setContextAndManager(Context context) {
        this.context = context;
        managerDevice = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    /**
     * This method returns a deep copy of all the information stored in this class' raw data.
     * @return returnData: A copy of the rawData in this class.
     */
    public ArrayList<String> getRawData() {
        ArrayList<String> returnData = new ArrayList<>();
        for (String entry:rawData) {
            returnData.add(new String(entry));
        }
        return returnData;
    }

    /*
    Class methods.
     */

    /**
     * This method initalizes the connection between the identified Usb device and the Android for serial communication.
     * Issues: As of current, we cannot call this method on the same String identifier twice, else it doesn't work.
     * @param deviceID The unique String identifier for a device connected to this Android.
     */
    public void initializeConnection(String deviceID) {
        HashMap<String, UsbDevice> deviceMap = managerDevice.getDeviceList();
        UsbDevice connectedDevice = deviceMap.get(deviceID);
        try {
            D2xxManager ftdiManager = D2xxManager.getInstance(context);
            ftdiManager.addUsbDevice(connectedDevice);
            boolean isFTDI = ftdiManager.isFtDevice(connectedDevice);
            if (isFTDI) {
                ftdiDevice = ftdiManager.openByUsbDevice(context, connectedDevice);
            }
            setArduinoSettings();                   //Todo: Future upgrades to this section, to be able to select the relevant Usb settings manually or something.
        }
        catch (Exception errorGettingInstance) {
            Log.d("Error Happened", errorGettingInstance.getMessage());
        }
    }

    /**
     * This method sets the device settings for a typical Arduino being plugged in.
     */
    private void setArduinoSettings() {
        if (getFTDIConnected()) {
            ftdiDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
            ftdiDevice.setBaudRate(9600);
            ftdiDevice.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
            ftdiDevice.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b, (byte) 0x0d);
        }
    }
    /**
     * This method tells you the names of all the devices connected to the Android device. (Gives back their unique String identifier).
     * @return A String array of the unique string identifiers of all the devices connected to this Android.
     */
    public String[] listDevices() {
        HashMap<String, UsbDevice> deviceMap = managerDevice.getDeviceList();
        Set<String> connectedUSBs = deviceMap.keySet();
        String[] listDevices = connectedUSBs.toArray(new String[connectedUSBs.size()]);
        return listDevices;
    }

    /**
     * This method resets the attached devices' data feed and this object's stored raw data, for a fresh reading.
     */
    public void newReadings() {
        getReading();
        ftdiDevice.resetDevice();
        rawData = new ArrayList<>();
    }
    /**
     * This method gets readings from the attached USB detector, and splices the incoming data stream to identify each
     * detection event, before appending the data sequentially in rawData.
     * @return Whether or not anything was read from the FTDI device.
     */
    public Boolean getReading() {
        if (ftdiDevice == null) {
            return false;
        }
        int bufferSize = ftdiDevice.getQueueStatus();
        byte[] dataBuffer = new byte[bufferSize];
        if ((ftdiDevice != null) && (bufferSize>0)) {
            ftdiDevice.read(dataBuffer, bufferSize);
            String data = new String(dataBuffer);
            String readings[] = data.split("\\r?\\n");
            for (String reading:readings) {
                rawData.add(reading);
            }
            return true;
        }
        return false;
    }

    /**
     * This method tells you if the attached FTDI device was successfully detected and connected.
     * Use after the initializeConnection() method.
     * @return Whether or not this manager is currently connected to a valid FTDI Usb device.
     */
    public Boolean getFTDIConnected() {
        return (ftdiDevice != null);
    }

    /**
     * This method tells the object to attain a fresh reading of the data in the USB.
     * @return gotReading: A boolean on if a reading was successfully attained.
     */
    public boolean freshReading() {
        boolean gotReading;
        newReadings();
        gotReading = getReading();
        return gotReading;
    }

    /**
     * This method tells you how many detection events were recorded.
     * @return The count of muon detection events recorded in rawData.
     */
    public int getNumberOfEvents() {
        return rawData.size();
    }

}