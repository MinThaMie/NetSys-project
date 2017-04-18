package com.nedap.university.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.LinkedList;

/**
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class FilePrep {
    static int dataSize = Statics.PACKETSIZE.value - Statics.HEADERLENGHT.value;

    static byte[] getBytesFromFile(File file){
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            System.out.println("byte length " + fileBytes.length);
            return fileBytes;
        } catch(IOException e){
            System.out.println("The file could not be read into a byte array!");
        }
        byte[] array = new byte[]{};
        return array;
    }

    static LinkedList<byte[]> getBytesToPacketSize(byte[] fileBytes){
        ByteBuffer fileBuffer = ByteBuffer.allocate(fileBytes.length).put(fileBytes);
        fileBuffer.rewind();
        LinkedList<byte[]> result = new LinkedList<>();
        int amountPackets = amountOfPacketsNeeded(fileBytes);
        for (int i= 0; i < amountPackets - 1; i++){
            byte[] partialArray = new byte[dataSize];
            fileBuffer.get(partialArray, 0, dataSize);
            result.add(partialArray);
        }
        int leftOverDatasize = fileBuffer.capacity() - fileBuffer.position();
        byte[] leftOver = new byte[leftOverDatasize];
        fileBuffer.get(leftOver, 0, leftOverDatasize);
        System.out.println("leftOver " + leftOver.length);
        result.add(leftOver);
        return result;
    }

    static int amountOfPacketsNeeded(byte[] fileBytes){
        return (int) Math.ceil(fileBytes.length/(double)(dataSize));
    }

    public static byte[] getByteArrayFromByteChunks(LinkedList<byte[]> receivedChunks){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        for(byte[] bytes : receivedChunks ){
            try {
                outputStream.write(bytes);
            } catch (IOException e){
                System.out.println("I could not write the bytes :(");
            }
        }
        byte[] result = outputStream.toByteArray();
        return result;
    }

    public static LinkedList<byte[]> filePrep(File file){
        return getBytesToPacketSize(getBytesFromFile(file));
    }

}
