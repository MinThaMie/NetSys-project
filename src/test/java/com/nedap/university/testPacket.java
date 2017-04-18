package com.nedap.university;
import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by anne-greeth.vanherwijnen on 18/04/2017.
 */
public class testPacket {
    private Packet packet;
    byte[] test;

    @Before
    public void Setup(){
        test = "this is a test".getBytes();
        packet = new Packet(9292,8288,new Flag[]{Flag.ACK}, 2, test);
    }

    @Test
    public void testData(){
        assertArrayEquals(test, packet.getData());
    }

}
