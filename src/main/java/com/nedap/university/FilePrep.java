package com.nedap.university;

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
        result.add(fileBuffer.array());
        return result;
    }

    static int amountOfPacketsNeeded(byte[] fileBytes){
        return (int) Math.ceil(fileBytes.length/(double)(dataSize));
    }

}
