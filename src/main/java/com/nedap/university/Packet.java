package com.nedap.university;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class Packet {
    private UDPHeader header;
    private byte[] data;
    private static int HEADERLENGTH = 13; //in Bytes
    public Packet(int sourceport, int destport, Flag[] flags, int seqNo, int ackNo, byte[] data){
        this.header = new UDPHeader(sourceport,destport,Flag.setFlags(flags), seqNo, ackNo ,data);
        this.data = data;
    }

    public Packet(UDPHeader header, byte[] data){
        this.header = header;
        this.data = data;
    }

    public static void main(String[] args) {
        byte[] mydata = "test".getBytes();
        Packet myPacket = new Packet( 8080, 9292, new Flag[]{Flag.ACK},1, 0, mydata);
        Packet testPacket = myPacket.bytesToPacket(getByteRepresentation(myPacket));
        testPacket.print();
    }


    /**
     * nested UPDHeader class which creates the UDPheader for the packer
     */

    public static byte[] getByteRepresentation(Packet packet){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(packet.getHeader().getHeaderByteRepresentation());
            outputStream.write(packet.getData());
        } catch (IOException e){
            System.out.println("Could not write this!");
        }

        return outputStream.toByteArray( );
    }

    public UDPHeader getHeader(){
        return this.header;
    }

    public byte[] getData(){
        return this.data;
    }

    public static Packet bytesToPacket(byte[] packet){
        byte[] headerBytes = Arrays.copyOfRange(packet, 0, HEADERLENGTH);
        UDPHeader header = headerBytesToHeader(headerBytes);
        byte[] data = Arrays.copyOfRange(packet, HEADERLENGTH, packet.length);
        return new Packet(header, data);
    }

    public static UDPHeader headerBytesToHeader(byte[] header){
        int sourcePort = Utils.bytesToInt(Arrays.copyOfRange(header, 0, 2));
        int destPort = Utils.bytesToInt(Arrays.copyOfRange(header, 2, 4));
        int udpLength = Utils.bytesToInt(Arrays.copyOfRange(header, 4, 6));
        int flags  = Utils.bytesToInt(Arrays.copyOfRange(header, 6, 7));
        int seqNo = Utils.bytesToInt(Arrays.copyOfRange(header, 7, 9));
        int ackNo = Utils.bytesToInt(Arrays.copyOfRange(header, 9, 11));
        int checksum = Utils.bytesToInt(Arrays.copyOfRange(header, 11, 13));
        return new UDPHeader(sourcePort, destPort, udpLength, flags, seqNo, ackNo, checksum);
    }

    public void print(){
        System.out.println("This packet: "  + this.getHeader().sourceport + " " +this.getHeader().destport + " " + this.getHeader().flags);
    }




}
