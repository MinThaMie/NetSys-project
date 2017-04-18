package com.nedap.university.packet;

/**
 * Flag ENUM to easily and obviously set and check flags
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
public enum Flag {
    SYN(1), ACK(2), FIN(4), FILES(8), PAUSE(16), DNS(32);
    int value;

    Flag(int value){
        this.value = value;
    }

    public static int setFlags(Flag[] flags){
        int flagsvalue = 0;
        for(Flag f : flags){
            flagsvalue += f.value;
        }
        return flagsvalue;
    }

    public static boolean isSet(Flag flag, int receivedFlagValue){
        return (receivedFlagValue & flag.value) != 0;
    }
}
