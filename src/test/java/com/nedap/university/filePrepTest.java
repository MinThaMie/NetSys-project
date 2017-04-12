package com.nedap.university;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class filePrepTest {

    private byte[] testArray1974 = new byte[1974];
    private byte[] testArray2013 = new byte[2013];
    private byte[] testArray4000 = new byte[4000];


    @Before

    public void SetUp(){

    }


    @Test
    public void testAmountOf() {
        assertThat(FilePrep.amountOfPacketsNeeded(testArray1974), is(2));
        assertThat(FilePrep.amountOfPacketsNeeded(testArray2013), is(3));
        assertThat(FilePrep.amountOfPacketsNeeded(testArray4000), is(5));
    }

    @Test
    public void testgetPartialArrays() {
        assertThat(FilePrep.getBytesToPacketSize(testArray1974).size(), is(2));
        assertThat(FilePrep.getBytesToPacketSize(testArray2013).size(), is(3));
        assertThat(FilePrep.getBytesToPacketSize(testArray4000).size(), is(5));
    }
}
