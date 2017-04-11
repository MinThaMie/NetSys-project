package com.nedap.university;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class UDPHeader{
    private static int HEADERLENGTH = 136; //17 bytes
    int UDPlength;
    int sourceport;
    int destport;
    int checksum;
    int flags;
    int seqNo;
    int ackNo;

    public UDPHeader(int sourceport, int destport, int flags, int seqNo, int ackNo, byte[] data){
        this.sourceport = sourceport; //16bit sourceport
        this.destport = destport; //16bit destport
        this.UDPlength = HEADERLENGTH + data.length;//16 bit UDPlength = UDP header + data
        this.flags = flags; // 8 bits flags
        this.seqNo = seqNo; //16 bits sequence number
        this.ackNo = ackNo; // 16 bits ack number
        this.checksum = 0; //TODO: implement
    }

    public UDPHeader(int sourcePort, int destPort, int udpLength, int flags, int seqNo, int ackNo, int checksum){
        this.sourceport = sourcePort;
        this.destport = destPort;
        this.UDPlength = udpLength;
        this.flags = flags;
        this.seqNo = seqNo;
        this.ackNo = ackNo;
        this.checksum = checksum;
    }

    public byte[] getHeaderByteRepresentation(){
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

    public byte[] get2ByteRepresentation(int value){
        byte[] result =  new byte[2];
        result[0] = (byte) (value >> 8);
        result[1] = (byte) value;
        return result;
    }

    public byte[] getByteFlags(int flags){
        byte[] byteFlags = new byte[1];
        byteFlags[0] = (byte) flags;
        return byteFlags;
    }

}

