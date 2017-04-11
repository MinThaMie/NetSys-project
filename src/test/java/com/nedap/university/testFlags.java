package com.nedap.university;

import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test file to test the Flag ENUM
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

public class testFlags {

    @Test
    public void testSetFlags() {
        assertThat(Flag.setFlags(new Flag[]{Flag.SYN}), is(1));
        assertThat(Flag.setFlags(new Flag[]{Flag.SYN, Flag.DNS}), is(33));
        assertThat(Flag.setFlags(new Flag[]{Flag.SYN, Flag.FIN, Flag.ACK}), is(7));

    }
}