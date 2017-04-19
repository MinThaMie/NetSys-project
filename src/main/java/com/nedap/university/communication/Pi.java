package com.nedap.university.communication;


import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;
import com.nedap.university.utils.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Pi class with all the functions and functionalities for the pi.
 * the class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

public class Pi  extends Thread{
    private static MulticastSocket broadCastSocket;
    private static DatagramSocket communicationSocket;
    private static boolean dnsIsSet = false;
    private static boolean isConnected = false;
    private static volatile boolean packetArrived = false;
    private static Receiver myReceiver;
    private static Sender mySender;
    private static SortedMap<Integer, byte[]> allByteChunks;
    private static Statistics myStatistics;

    private Pi(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this);
        allByteChunks = new TreeMap<>();
        myStatistics = new Statistics();
    }

    public static void init(){
        int COMMUNICATION_PORT = 9292;
        try{
            isConnected = true;
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.getValue());
            communicationSocket = new DatagramSocket(COMMUNICATION_PORT);
            Timeout.Start();
            Pi pi = new Pi();
            pi.start();
            myReceiver.start();
            mySender.start();
        }catch(IOException e){
            e.getMessage();
        }

        while(!dnsIsSet) {
            try {
                byte[] buf = new byte[Statics.PACKETSIZE.getValue()];
                DatagramPacket received = new DatagramPacket(buf, buf.length);
                broadCastSocket.receive(received);
                inspectPacket(received);
            } catch (IOException e) {
                System.out.println("Error");
            }
        }
    }

    public void run(){
        while(isConnected) {
            if (packetArrived){
                inspectPacket(myReceiver.getFirstPacket());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("finished");
    }

    private static String[] getFiles(){
        String filePath = "home/pi/files";
        File file = new File(filePath);
        return file.list();
    }

    DatagramSocket getCommunicationSocket(){
        return communicationSocket;
    }

    void packetAvailable(boolean bool){
        packetArrived = bool;
    }

    private static void inspectPacket(DatagramPacket received){
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();

            //mySender.setSeq(header.getSeqNo());
            if (Flag.isSet(Flag.DNS, header.getFlags()) && !Flag.isSet(Flag.ACK, header.getFlags())) {
                myReceiver.setLastFrameReceived(header.getSeqNo());
                mySender.setInitialSeqandAck(header.getSeqNo());
                System.out.println("Received DNS request and reply");
                mySender.setDestAddress(received.getAddress());
                mySender.setDestPort(received.getPort());
                mySender.sendDNSReply();
            }

            if (Flag.isSet(Flag.ABORT, header.getFlags())){
                System.out.println("Should abort the download");
                mySender.abortDownload();
            }

            if (Flag.isSet(Flag.DNS, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())) {
                dnsIsSet = true;
            }
            if (Flag.isSet(Flag.ACK, header.getFlags())) { //Received ack, so can stop the timeout for that packet
                mySender.setReceivedAck(receivedPacket);
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.SYN, header.getFlags())){
                String[] files = getFiles();
                String allFilesString = Utils.stringArrayToString(files);
                mySender.sendFileListReply(allFilesString.getBytes(), header.getSeqNo());
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.REQUEST, header.getFlags())){
                String[] files = getFiles();
                int requestFileIndex = Integer.parseInt(new String(receivedPacket.getData(), StandardCharsets.UTF_8));
                String selectedFile = files[requestFileIndex];
                File fileToSend = new File("home/pi/files/" + selectedFile);
                mySender.sendSimpleReply(header.getSeqNo());
                mySender.sendFile(fileToSend, Utils.createSha1(fileToSend));
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && !Flag.isSet(Flag.FIN, header.getFlags())) {
                receiveFileChunks(receivedPacket.getHeader().getSeqNo(), receivedPacket.getData()); //TODO: make sure this builds a good file when getting more chunks
                mySender.sendSimpleReply(header.getSeqNo());
            }

            //TODO: implement a time-out resend for the FinACK //TODO: Set Fin unit to stop receiving fins
            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.FIN, header.getFlags())) {
                System.out.println("I received the end");
                buildReceivedFile(receivedPacket.getData());
                mySender.sendSimpleReply(header.getSeqNo());
            }
        //receivedPacket.print();

    }


    private static void receiveFileChunks(Integer seqNo, byte[] data){
        if (allByteChunks.size() == 0){
            myStatistics.setStartTime(new Date());
        }
        if (!allByteChunks.containsKey(seqNo)) {
            allByteChunks.put(seqNo, data);
        }
    }

    private static void buildReceivedFile(byte[] receveidCheckSum){
        myStatistics.setEndTime(new Date());
        int id = new Random().nextInt(100);
        Utils.setFileContentsPi(allByteChunks, id , "png");
        byte[] calculatedChecksum;
        File receivedfile = new File(String.format("home/pi/files/plaatje%d.png", id));
        calculatedChecksum = Utils.createSha1(receivedfile);
        myStatistics.calculateSpeed(allByteChunks.size(), receivedfile.length());
        System.out.println("Got checksum from plaatje " + id);
        System.out.println("The receivedChecksum = " + Arrays.toString(receveidCheckSum));
        System.out.println("The calculatedChecksum = " + Arrays.toString(calculatedChecksum));
        System.out.println("The checksums are " + Utils.checkChecksum(receveidCheckSum, calculatedChecksum));
        allByteChunks.clear(); //Clear the map to be ready to receive a new file
    }

}
