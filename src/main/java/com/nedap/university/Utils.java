package com.nedap.university;

import java.util.Formatter;

/**
 * Utils class with tiny helpful functions
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class Utils {
    public static String bytesToHex(byte[] bytes){
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b); //TODO: find out how this works
        }
        return formatter.toString();
    }


    public static int bytesToInt(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff); //TODO: Find out how this works
        }
        return (int) value;
    }

    public static byte[] StringArraytoByteArray(String[] strings){
        return new byte[]{};
    }

}
