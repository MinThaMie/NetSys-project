package com.nedap.university;

import com.nedap.university.packet.Flag;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test file to test the Flag ENUM
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

public class testFlags {

    @Test
    public void testSetFlags() {
        assertThat(Flag.setFlags(new Flag[]{Flag.ACK}), is(2));
        assertThat(Flag.setFlags(new Flag[]{Flag.ACK, Flag.DNS}), is(34));
        assertThat(Flag.setFlags(new Flag[]{Flag.PAUSE, Flag.FIN, Flag.ACK}), is(22));
    }

    @Test
    public void isFlagSetTest(){
        assertFalse((Flag.isSet(Flag.FILES, 12) && !Flag.isSet(Flag.FIN, 12)));
        assertTrue((Flag.isSet(Flag.FILES, 8) && !Flag.isSet(Flag.FIN, 8)));
        assertFalse((Flag.isSet(Flag.FILES, 8) && Flag.isSet(Flag.FIN, 8)));
        assertTrue((Flag.isSet(Flag.FILES, 12) && Flag.isSet(Flag.FIN, 12)));
    }
}
