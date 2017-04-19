package com.nedap.university.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class can prep a file for sending
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class FilePrep {
    private static int dataSize = Statics.PACKETSIZE.value - Statics.HEADERLENGTH.value;

    static byte[] getBytesFromFile(File file){
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return fileBytes;
        } catch(IOException e){
            System.out.println("The file could not be read into a byte array!");
        }
        return new byte[]{};
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
        result.add(leftOver);
        return result;
    }

    static int amountOfPacketsNeeded(byte[] fileBytes){
        return (int) Math.ceil(fileBytes.length/(double)(dataSize));
    }

    public static SortedMap<Integer, byte[]> getByteMapFromByteChunks(LinkedList<byte[]> receivedChunks){
        SortedMap<Integer, byte[]> resultMap = new TreeMap<>();
        int seqNo = 0;
        for(byte[] bytes : receivedChunks ){
            resultMap.put(seqNo, bytes);
            seqNo++;
        }
        return resultMap;
    }

    public static LinkedList<byte[]> filePrep(File file){
        return getBytesToPacketSize(getBytesFromFile(file));
    }

}
