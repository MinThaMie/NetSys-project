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
    String sourceAddress;
    int UDPlength;
    int sourceport;
    int destport;
    int checksum;
    int flags;
    int seqNo;
    int ackNo;
    public UDPHeader(String sourceAddress, int sourceport, int destport, int flags, byte[] data){
        this.sourceAddress = sourceAddress; // 32 bit sourceAddress
        this.sourceport = sourceport; //16bit sourceport
        this.destport = destport; //16bit destport
        this.UDPlength = HEADERLENGTH + data.length;//16 bit UDPlength = UDP header + data
        this.flags = flags; // 8 bits flags
        this.seqNo = 0; //16 bits sequence number
        this.ackNo = 0; // 16 bits ack number
        this.checksum = 0; //TODO: implement
    }

    public UDPHeader(String sourceAddress, int sourcePort, int destPort, int udpLength, int flags, int seqNo, int ackNo, int checksum){
        this.sourceAddress = sourceAddress;
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
        byte[] sourceAddress = getByteAddress(this.sourceAddress);

        try {
            outputStream.write(sourceAddress);
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

    public byte[] getByteAddress(String sourceAddress){
        byte[] result = new byte[4];
        try {
            InetAddress ip = InetAddress.getByName(sourceAddress);
            result = ip.getAddress();
        } catch (UnknownHostException e) {
            System.out.println("The host is unknown");
        }
        return result;

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

