package com.nedap.university.utils;

import java.util.Date;

/**
 * Some minor statistics
 * Created by anne-greeth.vanherwijnen on 19/04/2017.
 */
public class Statistics {
    private long startTime;
    private long endTime;
    public Statistics(){

    }

    public void setStartTime(Date time){
        this.startTime = time.getTime();
    }

    public void setEndTime(Date time) {
        this.endTime = time.getTime();
    }

    public void calculateSpeed(int noPackets, long fileSize){
        long time = this.endTime - this.startTime;
        double timePerSecond = (time/1000.0) / noPackets;
        double transferSpeed = (fileSize/1000)/ (time/1000.0);
        System.out.println("The amount of seconds per packet is: " + timePerSecond + " transferspeed in KB/s: " +  transferSpeed);
    }
}
