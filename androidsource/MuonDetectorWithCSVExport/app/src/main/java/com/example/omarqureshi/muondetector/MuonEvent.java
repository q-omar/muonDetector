package com.example.omarqureshi.muondetector;

import java.util.Date;
/** Contains data for an individual muon event.
*/
public class MuonEvent {
    private int number;
    private String location;
    private String timestamp;
    private String rawData;

    public MuonEvent(int num, String loc, String eventTime, String rawData) {
        number = num;
        location = loc;
        timestamp = eventTime;
        this.rawData = rawData;
    }
    public MuonEvent(MuonEvent toBeCopied) {
        number = toBeCopied.getNumber();
        location = toBeCopied.getLocation();
        timestamp = toBeCopied.getTimestamp();
        rawData = toBeCopied.getRawData();
    }

    public int getNumber() {
        return number;
    }
    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public String getRawData() {
        return rawData;
    }
    public String getNumberAsString() {
        return "" + number;
    }

    public String getTimestampAsString() {
        return timestamp.toString();
    }

    public String toString() {
        return Integer.toString(number) + "&" + location + "&" + timestamp + "&" + rawData;
    }
    public static MuonEvent toMuonEvent(String muonEventEncoding) {
        String[] parts = muonEventEncoding.split("&");
        return new MuonEvent(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3]);
    }
}
