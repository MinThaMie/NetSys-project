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
    static String bytesToHex(byte[] bytes){
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

    public static long bytesToLong(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff); //TODO: Find out how this works
        }
        return value;
    }

    public static String stringArrayToString(String[] sArray) {
        return String.join(" ", sArray);
    }

    static String[] splitString(String toBeSplit, String splitBy){
        return toBeSplit.split(splitBy);
    }


    public static int updateSeq(UDPHeader header){
        return header.getSeqNo() + 1;
    }

    public static void setFileContentsPi(byte[] fileContents, int id, String format) {
        File fileToWrite;
        if (format.equals("jpg")) {
            fileToWrite = new File("home/pi/files/" + String.format("plaatje%d.jpg", id));//is Client path
        } else {
            fileToWrite = new File("home/pi/files/" + String.format("plaatje%d.png", id));//is Client path
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the client");
        }
    }

    static void setFileContentsClient(byte[] fileContents, int id, String format) {
        File fileToWrite;
        if (format.equals("jpg")) {
             fileToWrite = new File("files/" + String.format("plaatje%d.jpg", id));//is Client path
        } else {
             fileToWrite = new File("files/" + String.format("plaatje%d.png", id));//is Client path
        }
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the client");
        }
    }

    public static byte[] createSha1(File file) throws IOException, NoSuchAlgorithmException{ //TODO: throws is niet zo netjes, maar heeft wel als voordeel dat alles wel bestaat
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
