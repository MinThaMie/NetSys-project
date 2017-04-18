package com.nedap.university.packet;
import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by anne-greeth.vanherwijnen on 18/04/2017.
 */
public class testPacket {
    private Packet packet;
    byte[] test;
    private byte[] bytePacket;

    @Before
    public void Setup(){
        test = "this is a test".getBytes();
        packet = new Packet(9292,8288,new Flag[]{Flag.ACK}, 2, test);
        bytePacket = packet.getByteRepresentation();
    }

    @Test
    public void testData(){
        assertArrayEquals(test, packet.getData());
    }


    @Test
    public void testChecksum(){
        Packet testPacket = Packet.bytesToPacket(bytePacket);
        assertTrue(testPacket.getHeader().checkChecksum());
        assertTrue(Utils.checkChecksum(packet.getHeader().getChecksum(),testPacket.getHeader().getChecksum()));
    }
}
