package com.nedap.university.communication;


import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;
import com.nedap.university.utils.FilePrep;
import com.nedap.university.utils.Statics;
import com.nedap.university.utils.Timeout;
import com.nedap.university.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
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

    private Pi(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this);
        allByteChunks = new TreeMap<>();
    }

    public static void init(){
        int COMMUNICATION_PORT = 9292;
        try{
            isConnected = true;
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.getValue());
            communicationSocket = new DatagramSocket(COMMUNICATION_PORT); //TODO: maybe move this to the run method and only when there is a dnsSet
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

            if (Flag.isSet(Flag.DNS, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())) {
                dnsIsSet = true;
            }
            if (Flag.isSet(Flag.ACK, header.getFlags())) { //Received ack, so can stop the timeout for that packet
                //System.out.println("received ack");
                mySender.setReceivedAck(receivedPacket);
                //mySender.sendSimpleReply(header.getSeqNo()); //TODO: remove this
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.SYN, header.getFlags())){
                String[] files = getFiles();
                String allFilesString = Utils.stringArrayToString(files);
                mySender.sendFileListReply(allFilesString.getBytes(), header.getSeqNo());
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.REQUEST, header.getFlags())){
                String[] files = getFiles();
                int requestFileIndex = Integer.parseInt(new String(receivedPacket.getData(), StandardCharsets.UTF_8));
                System.out.println("files length " + files.length + " requested " + requestFileIndex);
                String selectedFile = files[requestFileIndex];
                File fileToSend = new File("home/pi/files/" + selectedFile);
                mySender.sendSimpleReply(header.getSeqNo());
                mySender.sendFile(fileToSend, Utils.createSha1(fileToSend));
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && !Flag.isSet(Flag.FIN, header.getFlags())) {
                //System.out.println("received file chunk with seqNo " + header.getSeqNo() + " checksum " + header.getChecksum());
                receiveFileChunks(receivedPacket.getHeader().getSeqNo(), receivedPacket.getData()); //TODO: make sure this builds a good file when getting more chunks
                mySender.sendSimpleReply(header.getSeqNo());
            }

            //TODO: implement a time-out resend for the FinACK
            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.FIN, header.getFlags())) {
                //System.out.println("I received the end");
                buildReceivedFile(receivedPacket.getData());
                mySender.sendSimpleReply(header.getSeqNo());
            }
        //receivedPacket.print();

    }


    private static void receiveFileChunks(Integer seqNo, byte[] data){ //TODO: Change this to a linkedList
        if (!allByteChunks.containsKey(seqNo)) {
            allByteChunks.put(seqNo, data);
        }
    }

    private static LinkedList<byte[]> mapToList(){
        LinkedList<byte[]> theList = new LinkedList<>();
        for(Integer key : allByteChunks.keySet()){
            theList.add(allByteChunks.get(key));
        }
        return theList;
    }
    private static void buildReceivedFile(byte[] receveidCheckSum){
        int id = new Random().nextInt(100);
        Utils.setFileContentsPi(FilePrep.getByteArrayFromByteChunks(mapToList()), id , "png");
        byte[] calculatedChecksum;
        calculatedChecksum = Utils.createSha1(new File(String.format("home/pi/files/plaatje%d.png", id)));
        System.out.println("Got checksum from plaatje " + id);
        System.out.println("The receivedChecksum = " + Arrays.toString(receveidCheckSum));
        System.out.println("The calculatedChecksum = " + Arrays.toString(calculatedChecksum));
        System.out.println("The checksums are " + Utils.checkChecksum(receveidCheckSum, calculatedChecksum));
        allByteChunks.clear(); //Clear the map to be ready to receive a new file
    }

}
