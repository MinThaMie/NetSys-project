package com.nedap.university.utils;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

import java.util.Date;

/**
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

    public double calculateSpeed(int noPackets){
        double speed = noPackets/((this.endTime - this.startTime)*1000);
        System.out.println("The amount of packets per second is: " + speed);
        return speed;
    }
}
