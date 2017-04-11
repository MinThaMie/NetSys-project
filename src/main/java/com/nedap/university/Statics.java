package com.nedap.university;

/**
 * Statics class to access the static values for ports and IP-address quick and easy across the files
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
public enum Statics {
    BROADCASTPORT(8080), BROADCASTADDRESS("192.168.40.255"), HEADERLENGHT(13);

    int value;
    String string;

    Statics(int value){
        this.value = value;
    }
    Statics(String string){
        this.string = string;
    }
}
