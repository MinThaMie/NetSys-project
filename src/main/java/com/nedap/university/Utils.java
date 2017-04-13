package com.nedap.university;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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


    static int bytesToInt(byte[] bytes){
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

    static byte[] setFileContentsPi(byte[] fileContents, int id) {
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

    static void setFileContentsClient(byte[] fileContents, int id) {
        File fileToWrite = new File(String.format("plaatje%d.jpg", id)); //is Client path
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the client");
        }
    }

    static byte[] createSha1(File file) throws IOException, NoSuchAlgorithmException{ //TODO: throws is niet zo netjes, maar heeft wel als voordeel dat alles wel bestaat
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

    static boolean checkChecksum(byte[] checksumReceived, byte[] checksumCalculated){
        return Arrays.equals(checksumReceived, checksumCalculated);
    }
}
