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
    private static int HEADERLENGTH = 136; //17 bytes
    public Packet(String sourceAdr, int sourceport, int destport, byte[] data){
        this.header = new UDPHeader(sourceAdr, sourceport,destport,data);
        this.data = data;
    }

    public Packet(UDPHeader header, byte[] data){
        this.header = header;
        this.data = data;
    }

    public static void main(String[] args) {
        byte[] mydata = "test".getBytes();
        Packet myPacket = new Packet("192.168.40.5", 8080, 9292, mydata);
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

    public Packet bytesToPacket(byte[] packet){
        byte[] headerBytes = Arrays.copyOfRange(packet, 0, HEADERLENGTH/8);
        UDPHeader header = headerBytesToHeader(headerBytes);
        System.out.println("packetlength " + packet.length);
        byte[] data = Arrays.copyOfRange(packet, HEADERLENGTH/8, packet.length);
        return new Packet(header, data);
    }

    public UDPHeader headerBytesToHeader(byte[] header){
        String sourceAddr= "";
        try {
            sourceAddr = InetAddress.getByAddress(Arrays.copyOfRange(header, 0, 4)).getHostAddress();
        } catch (UnknownHostException e){
            System.out.println("The host does not exist or the computation from bytes to address went wrong");
        }
        int sourcePort = Utils.bytesToInt(Arrays.copyOfRange(header, 4, 6));
        int destPort = Utils.bytesToInt(Arrays.copyOfRange(header, 6, 8));
        int udpLength = Utils.bytesToInt(Arrays.copyOfRange(header, 8, 10));
        int flags  = Utils.bytesToInt(Arrays.copyOfRange(header, 10, 11));
        int seqNo = Utils.bytesToInt(Arrays.copyOfRange(header, 11, 13));
        int ackNo = Utils.bytesToInt(Arrays.copyOfRange(header, 13, 15));
        int checksum = Utils.bytesToInt(Arrays.copyOfRange(header, 15, 17));
        UDPHeader newHeader = new UDPHeader(sourceAddr,sourcePort, destPort, udpLength, flags, seqNo, ackNo, checksum);
        return newHeader;
    }

    public void print(){
        System.out.println("This packet: " + this.getHeader().sourceAddress + " " + this.getHeader().sourceport + " " +this.getHeader().destport);
    }


}
