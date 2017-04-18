package com.nedap.university.utils;

import com.nedap.university.utils.FilePrep;
import com.nedap.university.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * These test check the deconstruction and reassembly of files.
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class filePrepTest {

    private byte[] testArray1974 = new byte[1974];
    private byte[] testArray2013 = new byte[2013];
    private byte[] testArray4000 = new byte[4000];
    private byte[] myData;
    private byte[] reassembledData;
    @Before

    public void SetUp(){
        myData = FilePrep.getBytesFromFile(new File("/Users/anne-greeth.vanherwijnen/NetSys-project/photo1.jpg"));
        LinkedList<byte[]> choppedData = FilePrep.getBytesToPacketSize(myData);
        reassembledData = FilePrep.getFileFromByteChunks(choppedData);
        //Utils.setFileContentsClient(reassembledData, 2); //This is commented out because it otherwise creates a picture every build
    }


    @Test
    public void testAmountOf() {
        assertThat(FilePrep.amountOfPacketsNeeded(testArray1974), is(2));
        assertThat(FilePrep.amountOfPacketsNeeded(testArray2013), is(2));
        assertThat(FilePrep.amountOfPacketsNeeded(testArray4000), is(3));
    }

    @Test
    public void testgetPartialArrays() {
        assertThat(FilePrep.getBytesToPacketSize(testArray1974).size(), is(2));
        assertThat(FilePrep.getBytesToPacketSize(testArray2013).size(), is(2));
        assertThat(FilePrep.getBytesToPacketSize(testArray4000).size(), is(3));
    }

    @Test
    public void testChopping() { //Assumption is that the same byteArray results in the same picture :)
        assertArrayEquals(reassembledData,myData);
    }
}
