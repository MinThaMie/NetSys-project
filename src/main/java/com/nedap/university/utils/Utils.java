package com.nedap.university.utils;

import com.nedap.university.packet.UDPHeader;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Utils class with tiny helpful functions
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class Utils {

    public static int bytesToInt(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return (int) value;
    }

    public static long bytesToLong(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }

    public static String stringArrayToString(String[] sArray) {
        return String.join(" ", sArray);
    }

    public static String[] splitString(String toBeSplit, String splitBy){
        return toBeSplit.split(splitBy);
    }

    public static void setFileContentsPi(SortedMap<Integer, byte[]> allChunks, int id, String format) {
        File fileToWrite;
        if (format.equals("jpg")) {
            fileToWrite = new File("home/pi/files/" + String.format("plaatje%d.jpg", id));//is Client path
        } else {
            fileToWrite = new File("home/pi/files/" + String.format("plaatje%d.png", id));//is Client path
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (Integer key : allChunks.keySet()) {
                fileStream.write(allChunks.get(key));
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the pi");
        }
    }

    public static void setFileContentsClient(SortedMap<Integer, byte[]> allChunks, int id, String format) {
        File fileToWrite;
        if (format.equals("jpg")) {
             fileToWrite = new File("files/" + String.format("plaatje%d.jpg", id));//is Client path
        } else {
             fileToWrite = new File("files/" + String.format("plaatje%d.png", id));//is Client path
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (Integer key : allChunks.keySet()) {
                fileStream.write(allChunks.get(key));
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the client");
        }
    }

    public static byte[] createSha1(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream fileInput = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fileInput.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e){
            System.out.println("There is no such thing as SHA-1");
        } catch (IOException e){
            System.out.println("I could not read or write the fileChecksum buffer");
        }
        System.out.println("Something went even more wrong and I returned an empty SHA");
        return new byte[]{};
    }

    public static boolean checkChecksum(byte[] checksumReceived, byte[] checksumCalculated){
        return Arrays.equals(checksumReceived, checksumCalculated);
    }

    public static boolean checkChecksum(long checksumReceived, long checksumCalculated){
        return checksumReceived == checksumCalculated;
    }

    public static long updCRCchecksum(byte[] bytes){
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        return checksum.getValue();
    }
}
