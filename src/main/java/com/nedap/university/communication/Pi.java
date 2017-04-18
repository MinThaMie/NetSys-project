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
    //static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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
                byte[] buf = new byte[1000];
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
        String filePath = "files";
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
        if (header.checkChecksum()) {
            mySender.setSeq(header.getSeqNo()); //TODO: Check this since this is the reason for the off-by-one error
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
            if (Flag.isSet(Flag.ACK, header.getFlags())) { //Received ack, so can stop the timeout for that packet //TODO: normally no reply on Ack
                //System.out.println("received ack");
                mySender.setReceivedAck(receivedPacket);
                //mySender.sendSimpleReply();
            }
            System.out.println("Header value " + header.getFlags());
            if (Flag.isSet(Flag.FILES, header.getFlags()) && !Flag.isSet(Flag.FIN, header.getFlags())) {
                System.out.println("received file chunk with seqNo " + header.getSeqNo() + " checksum " + header.getChecksum());
                receiveFileChunks(receivedPacket.getHeader().getSeqNo(), receivedPacket.getData()); //TODO: make sure this builds a good file when getting more chunks
                mySender.sendSimpleReply();
            }

            if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.FIN, header.getFlags())) {
                //System.out.println("I received the end");
                buildReceivedFile(receivedPacket.getData());
                mySender.sendSimpleReply();
            }
        } else {
            System.out.println("received corrupt packet :(");
        }
        //receivedPacket.print();
    }

    private static void receiveFileChunks(Integer seqNo, byte[] data){ //TODO: Change this to a linkedList
        if (!allByteChunks.containsKey(seqNo)) {
            System.out.println("data " + Arrays.toString(data));
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
        byte[] calculatedChecksum = new byte[20];
        try {
            calculatedChecksum = Utils.createSha1(new File(String.format("home/pi/files/plaatje%d.png", id)));
            System.out.println("Got checksum from plaatje " + id);
        } catch (NoSuchAlgorithmException e){
            System.out.println("No SHA");
        } catch (IOException e){
            System.out.println("Could not write");
        }
        System.out.println("The receivedChecksum = " + Arrays.toString(receveidCheckSum));
        System.out.println("The calculatedChecksum = " + Arrays.toString(calculatedChecksum));
        System.out.println("The checksums are " + Utils.checkChecksum(receveidCheckSum, calculatedChecksum));
    }

}
