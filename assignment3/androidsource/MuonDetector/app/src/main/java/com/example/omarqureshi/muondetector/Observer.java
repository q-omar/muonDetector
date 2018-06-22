package com.example.omarqureshi.muondetector;

public interface Observer {
    public Processor processor = null; //subject class that the observers are watching, change as required
    public abstract void update();
 }