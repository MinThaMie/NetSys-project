package com.nedap.university;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Pi class with all the functions and functionalities for the pi.
 * the class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

class Pi  extends Thread{
    private static MulticastSocket broadCastSocket;
    private static DatagramSocket communicationSocket;
    private static boolean dnsIsSet = false;
    private static boolean isConnected = false;
    private static volatile boolean packetArrived = false;
    private static Receiver myReceiver;
    private static Sender mySender;
    static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    private Pi(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this.getCommunicationSocket());
    }

    static void init(){
        int COMMUNICATION_PORT = 9292;
        try{
            isConnected = true;
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.value);
            communicationSocket = new DatagramSocket(COMMUNICATION_PORT); //TODO: maybe move this to the run method and only when there is a dnsSet
            Pi pi = new Pi();
            pi.start();
            myReceiver.start();
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
        System.out.println(file.list().length);
        return file.list();
    }

    private static int[] getSeqAndAck(UDPHeader header){
        int[] result = new int[2];
        result[0] = header.getSeqNo(); //get seqNo
        result[1] = header.getAckNo(); //get ackNo
        return result;
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
        if(Flag.isSet(Flag.DNS,header.getFlags()) && !Flag.isSet(Flag.ACK,header.getFlags())){
            System.out.println("Received DNS request and reply");
            mySender.setDestAddress(received.getAddress());
            mySender.setDestPort(received.getPort());
            mySender.sendDNSReply();
        }

        if (Flag.isSet(Flag.DNS, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())){
            dnsIsSet = true;
        }
        if (Flag.isSet(Flag.ACK, header.getFlags())) {
            int[] seqAndAck = getSeqAndAck(header);
            mySender.sendSimpleReply(seqAndAck);
        }

        if (Flag.isSet(Flag.FILES, header.getFlags()) && !Flag.isSet(Flag.FIN, header.getFlags())){
            receiveFileChunks(receivedPacket.getData());
        }

        if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.FIN, header.getFlags())){
            buildReceivedFile(receivedPacket.getData());
        }
        //receivedPacket.print();
    }

    static void receiveFileChunks(byte[] data){
        try {
            outputStream.write(data);
        } catch(IOException e){
            System.out.println("could not write to stream");
        }
    }

    static void buildReceivedFile(byte[] receveidCheckSum){
        byte[] calculatedChecksum = Utils.setFileContentsPi(outputStream.toByteArray(), 3);
        System.out.println("The checksums are " + Utils.checkChecksum(receveidCheckSum, calculatedChecksum));
    }

}
