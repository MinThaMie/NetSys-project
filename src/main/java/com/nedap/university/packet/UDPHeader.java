package com.nedap.university.packet;

import com.nedap.university.utils.Statics;
import com.nedap.university.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * UDP header class to build a my own header to go on top of the data including ARQ implementation
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class UDPHeader{
    private int sourceport;
    private int destport;
    private int UDPlength;
    private int flags;
    private int seqNo;
    private long checksum;

    UDPHeader(int sourceport, int destport, int flags, int seqNo, byte[] data){
        this.sourceport = sourceport; //16bit sourceport
        this.destport = destport; //16bit destport
        this.UDPlength = Statics.HEADERLENGTH.getValue() + data.length;//16 bit UDPlength = UDP header + data
        this.flags = flags; // 8 bits flags
        this.seqNo = seqNo; //16 bits sequence number
        this.checksum = Utils.updCRCchecksum(getHeaderByteRepresentationWithoutChecksum(this));
    }

    UDPHeader(int sourcePort, int destPort, int udpLength, int flags, int seqNo, long checksum){
        this.sourceport = sourcePort;
        this.destport = destPort;
        this.UDPlength = udpLength;
        this.flags = flags;
        this.seqNo = seqNo;
        this.checksum = checksum;
    }

    private byte[] getHeaderByteRepresentationWithoutChecksum(UDPHeader header){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write((get2ByteRepresentation(header.getSourceport())));
            outputStream.write((get2ByteRepresentation(header.getDestport())));
            outputStream.write((get2ByteRepresentation(header.getUDPlength())));
            outputStream.write((getByteFlags(header.getFlags())));
            outputStream.write((get2ByteRepresentation(header.getSeqNo())));
        } catch (IOException e){
            System.out.println("Could not write header without checksum!");
        }
        return outputStream.toByteArray( );
    }

    byte[] getHeaderByteRepresentationWithChecksum(UDPHeader header){
        byte[] bytes = getHeaderByteRepresentationWithoutChecksum(header);
        long checksum = Utils.updCRCchecksum(bytes);
        this.checksum = checksum;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bytes);
            outputStream.write(get4ByteRepresentation(checksum));
        } catch (IOException e){
            System.out.println("Could not write the header with checksum");
        }
        return outputStream.toByteArray( );
    }

    public boolean checkChecksum(){
        byte[] headerBytes = getHeaderByteRepresentationWithoutChecksum(this);
        long calculatedChecksum = Utils.updCRCchecksum(headerBytes);
        return calculatedChecksum == this.getChecksum();
    }

    private byte[] get2ByteRepresentation(int value){
        byte[] result =  new byte[2];
        result[0] = (byte) (value >> 8);
        result[1] = (byte) value;
        return result;
    }

    private byte[] get4ByteRepresentation(long value){
        byte[] result =  new byte[4];
        result[0] = (byte) (value >> 24);
        result[1] = (byte) (value >> 16);
        result[2] = (byte) (value >> 8);
        result[3] = (byte) value;
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

    public int getFlags(){
        return this.flags;
    }

    public int getSeqNo(){
        return this.seqNo;
    }

    public long getChecksum(){
        return this.checksum;
    }
}

