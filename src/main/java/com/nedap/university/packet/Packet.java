package com.nedap.university.packet;

import com.nedap.university.utils.Statics;
import com.nedap.university.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Packet class used to build the packets, including translation to and from bytes
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */

public class Packet {
    private UDPHeader header;
    private byte[] data;

    public Packet(int sourceport, int destport, Flag[] flags, int seqNo, byte[] data){
        this.header = new UDPHeader(sourceport,destport,Flag.setFlags(flags), seqNo,data);
        this.data = data;
    }

    private Packet(UDPHeader header, byte[] data){
        this.header = header;
        this.data = data;
    }

    public static void main(String[] args) {
        byte[] mydata = "".getBytes();
        Packet myPacket = new Packet( 8080, 9292, new Flag[]{Flag.ACK},1, mydata);
        Packet testPacket = bytesToPacket(myPacket.getByteRepresentation());
        testPacket.print();
    }

    public byte[] getByteRepresentation(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(this.getHeader().getHeaderByteRepresentationWithChecksum(this.getHeader()));
            outputStream.write(this.getData());
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
        byte[] headerBytes = Arrays.copyOfRange(packet, 0, Statics.HEADERLENGTH.getValue());
        UDPHeader header = headerBytesToHeader(headerBytes);
        byte[] data = Arrays.copyOfRange(packet, Statics.HEADERLENGTH.getValue(), header.getUDPlength());
        return new Packet(header, data);
    }

    private static UDPHeader headerBytesToHeader(byte[] header){
        int sourcePort = Utils.bytesToInt(Arrays.copyOfRange(header, 0, 2));
        int destPort = Utils.bytesToInt(Arrays.copyOfRange(header, 2, 4));
        int udpLength = Utils.bytesToInt(Arrays.copyOfRange(header, 4, 6));
        int flags  = Utils.bytesToInt(Arrays.copyOfRange(header, 6, 7));
        int seqNo = Utils.bytesToInt(Arrays.copyOfRange(header, 7, 9));
        long checksum = Utils.bytesToLong(Arrays.copyOfRange(header, 9, 13));
        return new UDPHeader(sourcePort, destPort, udpLength, flags, seqNo, checksum);
    }

    public void print(){
        System.out.println("This packet: "  + this.getHeader().getSourceport() + " " +this.getHeader().getDestport() + ", seqNo " + this.getHeader().getSeqNo() +
        "udpLength " + this.getHeader().getUDPlength() + '\n' + " flags " + this.getHeader().getFlags() + " checksum " + this.getHeader().getChecksum());
    }




}
