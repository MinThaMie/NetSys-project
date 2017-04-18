package com.nedap.university.utils;

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
    private File originalFileJPG;
    private File reassembeldFileJPG;
    private byte[] myDataPNG;
    private byte[] reassembledDataPNG;
    private File originalFilePNG;
    private File reassembeldFilePNG;
    //@Before
//TODO: uncomment
//    public void SetUp(){
//        originalFileJPG = new File("files/photo1.jpg");
//        myData = FilePrep.getBytesFromFile(originalFileJPG);
//        LinkedList<byte[]> choppedData = FilePrep.getBytesToPacketSize(myData);
//        reassembledData = FilePrep.getByteArrayFromByteChunks(choppedData);
//        Utils.setFileContentsClient(reassembledData, 3, "jpg"); //This is commented out because it otherwise creates a picture every build
//        reassembeldFileJPG = new File("files/plaatje3.jpg");
//
//        //PNG test
//        originalFilePNG = new File("files/rdtcInput3.png");
//        myDataPNG = FilePrep.getBytesFromFile(originalFilePNG);
//        LinkedList<byte[]> choppedDataPNG = FilePrep.getBytesToPacketSize(myDataPNG);
//        reassembledDataPNG = FilePrep.getByteArrayFromByteChunks(choppedDataPNG);
//        Utils.setFileContentsClient(reassembledDataPNG, 3, "png"); //This is commented out because it otherwise creates a picture every build
//        reassembeldFilePNG = new File("files/plaatje3.png");
//    }


//    @Test
//    public void testAmountOf() {
//        assertThat(FilePrep.amountOfPacketsNeeded(testArray1974), is(2));
//        assertThat(FilePrep.amountOfPacketsNeeded(testArray2013), is(2));
//        assertThat(FilePrep.amountOfPacketsNeeded(testArray4000), is(3));
//    }
//
//    @Test
//    public void testgetPartialArrays() {
//        assertThat(FilePrep.getBytesToPacketSize(testArray1974).size(), is(2));
//        assertThat(FilePrep.getBytesToPacketSize(testArray2013).size(), is(2));
//        assertThat(FilePrep.getBytesToPacketSize(testArray4000).size(), is(3));
//    }
//
//    @Test
//    public void testChopping() { //Assumption is that the same byteArray results in the same picture :)
//        assertArrayEquals(reassembledData,myData);
//    }

//    @Test
//    public void testCheckSumJPG() throws Exception{
//        assertArrayEquals(Utils.createSha1(originalFileJPG), Utils.createSha1(reassembeldFileJPG));
//    }
//    @Test
//    public void testCheckSumPNG() throws Exception{
//        assertArrayEquals(Utils.createSha1(originalFilePNG), Utils.createSha1(reassembeldFilePNG));
//    }
}
