package com.nedap.university;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * UDP header class to build a my own header to go on top of the data including ARQ implementation
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
class UDPHeader{
    private int sourceport;
    private int destport;
    private int UDPlength;
    private int flags;
    private int seqNo;
    private int ackNo;
    private int checksum;

    UDPHeader(int sourceport, int destport, int flags, int seqNo, int ackNo, byte[] data){
        int headerLength = 17; //17 bytes
        this.sourceport = sourceport; //16bit sourceport
        this.destport = destport; //16bit destport
        this.UDPlength = headerLength + data.length;//16 bit UDPlength = UDP header + data
        this.flags = flags; // 8 bits flags
        this.seqNo = seqNo; //16 bits sequence number
        this.ackNo = ackNo; // 16 bits ack number
        this.checksum = 0; //TODO: implement
    }

    UDPHeader(int sourcePort, int destPort, int udpLength, int flags, int seqNo, int ackNo, int checksum){
        this.sourceport = sourcePort;
        this.destport = destPort;
        this.UDPlength = udpLength;
        this.flags = flags;
        this.seqNo = seqNo;
        this.ackNo = ackNo;
        this.checksum = checksum;
    }

    byte[] getHeaderByteRepresentation(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write((get2ByteRepresentation(sourceport)));
            outputStream.write((get2ByteRepresentation(destport)));
            outputStream.write((get2ByteRepresentation(UDPlength)));
            outputStream.write((getByteFlags(flags)));
            outputStream.write((get2ByteRepresentation(seqNo)));
            outputStream.write((get2ByteRepresentation(ackNo)));
            outputStream.write((get2ByteRepresentation(checksum)));
        } catch (IOException e){
            System.out.println("Could not write this!");
        }
        return outputStream.toByteArray( );
    }

    private byte[] get2ByteRepresentation(int value){
        byte[] result =  new byte[2];
        result[0] = (byte) (value >> 8);
        result[1] = (byte) value;
        return result;
    }

    private byte[] getByteFlags(int flags){
        byte[] byteFlags = new byte[1];
        byteFlags[0] = (byte) flags;
        return byteFlags;
    }

    int getSourceport(){
        return this.sourceport;
    }

    int getDestport(){
        return this.destport;
    }

    int getUDPlength(){
        return this.UDPlength;
    }

    int getFlags(){
        return this.flags;
    }

    int getSeqNo(){
        return this.seqNo;
    }

    int getAckNo(){
        return this.ackNo;
    }

    int getChecksum(){
        return this.checksum;
    }
}

