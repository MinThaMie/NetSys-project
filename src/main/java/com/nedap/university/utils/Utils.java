package com.nedap.university.utils;

import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.locks.ReentrantLock;

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

    public static String stringArrayToString(String[] sArray) {
        return String.join(" ", sArray);
    }

    public static String[] splitString(String toBeSplit, String splitBy){
        return toBeSplit.split(splitBy);
    }


    //TODO: PROBABLY NOT NEEDED ANYMORE because i use just + 1 for my seq and acks
    private static int[] getSeqAndAck(UDPHeader header){
        int[] result = new int[2];
        result[0] = header.getSeqNo(); //get seqNo
        result[1] = header.getAckNo(); //get ackNo
        System.out.println("received seq and ack " + result[0] + " " + result[1]);
        return result;
    }

    public static int[] updateSeqAndAck(UDPHeader header){
        int[] array = getSeqAndAck(header);
        int[] result = new int[2];
        result[0] = array[1]; //Sequence number is the ack from the previous packet
        result[1] = array[0] + 1; //Ack number is the sequence number + 1
        System.out.println("updated seq + ack" + result[0] + " " + result[1]);
        return result;
    }

    public static byte[] setFileContentsPi(byte[] fileContents, int id) {
        File fileToWrite = new File(String.format("/home/pi/files/plaatje%d.jpg", id)); //IS piPath
        byte[] result = new byte[]{};
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e) {
            System.out.println("Could not write the file on the pi");;
        }
        try {
            result = createSha1(fileToWrite);
        } catch (NoSuchAlgorithmException e){
            System.out.println("Your algorithm is not correct");
        } catch (IOException e){
            System.out.println("Could not write sha to stream");
        }
        return result;
    }

    public static void setFileContentsClient(byte[] fileContents, int id) {
        File fileToWrite = new File("files/" + String.format("plaatje%d.jpg", id)); //is Client path //TODO: Check if this path works
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
}
