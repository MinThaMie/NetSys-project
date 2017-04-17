package com.nedap.university;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is used to send packets
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
class Sender extends Thread implements ITimeoutEventHandler{

    private static int myPort;
    private DatagramSocket mySocket;
    private static int destPort;
    private static InetAddress destAddress;
    private boolean isSending;
    ConcurrentLinkedQueue<Packet> queue; //TODO: this assumes that there is one UDP connection
    private static int lastAckReceived = -1;
    private static int slidingWindowSize = 5;
    private static int lastFrameSend = -1;
    private int seqNo = 0;
    private int ackNo = 1;


    Sender(DatagramSocket socket){
        this.mySocket = socket;
        this.queue = new ConcurrentLinkedQueue<>(); //TODO: Check if this needs to be concurrent

    }

    public void run(){
        init();
        System.out.println("LFS " + lastFrameSend + " LAR " + lastAckReceived + " sws " +slidingWindowSize );
        while(isSending) {
            while (lastFrameSend < lastAckReceived + slidingWindowSize) {
                if(queue.size() > 0) {
                    Packet result = queue.remove();
                    sendPacket(result);
                    setTimeOutforPacket(result);
                    lastFrameSend = result.getHeader().getSeqNo();
                    seqNo = seqNo + 1; // this is the previous ackNo;
                    ackNo = ackNo + 1;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("I got interrupted");
            }
        }
    }

    private void init(){
        myPort = mySocket.getLocalPort(); //TODO: getLocalport does not work for the pi for some reason
        isSending = true;
    }

    private void sendPacket(Packet packet){
        byte[] myBytes = Packet.getByteRepresentation(packet);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, destAddress, destPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendDNSRequest(){ //TODO: Find out if there is a way to do this also with the sendPacket function
        Packet myPacket = new Packet(myPort,Statics.BROADCASTPORT.value, new Flag[]{Flag.DNS}, this.seqNo, this.ackNo, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(Statics.BROADCASTADDRESS.string), Statics.BROADCASTPORT.value));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendDNSReply(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, this.ackNo, new byte[]{});
        queue.add(myPacket);
        System.out.println("added really to the queue " + queue.size());
    }

    void sendDNSAck(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, this.ackNo, new byte[]{});
        queue.add(myPacket);
    }

    void sendFileFin(byte[] checksum){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.FIN}, this.seqNo, this.ackNo, checksum);
        queue.add(myPacket);
    }

    void sendFileRequest(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, this.seqNo, this.ackNo, new byte[]{});
        queue.add(myPacket);
    }

    void sendSimpleReply(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.ACK}, this.seqNo, this.ackNo, new byte[]{});
        queue.add(myPacket);
    }

    void sendFile(File file, byte[] checksum){
        LinkedList<byte[]> dataChunks = FilePrep.filePrep(file);
        int dataChunkPointer = 0;
        while(dataChunkPointer < dataChunks.size()) {
            sendFileChuck(dataChunks.get(dataChunkPointer));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            dataChunkPointer++;
        }
        sendFileFin(checksum);
    }

    private void sendFileChuck(byte[] fileChunk){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, this.seqNo, this.ackNo, fileChunk);
        queue.add(myPacket);
    }

    public void TimeoutElapsed(Packet packet) {
        System.out.println("Should resend something");
            sendPacket(packet);
            System.out.println("Resent packet with ackNo: " + packet.getHeader().getAckNo()); //Does not need to waitForAck, cause it's already waiting
            setTimeOutforPacket(packet);
    }

    private void setTimeOutforPacket(Packet sendPacket) {
        // schedule a timer for 1000 ms into the future, just to show how that works:
        Utils.Timeout.SetTimeout(1000, this, sendPacket);
    }

    void setReceivedAck(Packet packet){
        Utils.Timeout.stopTimeOut(packet);
        lastAckReceived = packet.getHeader().getAckNo();
    }

    void setDestPort(int port){
        destPort = port;
    }

    void setDestAddress(InetAddress address){
        destAddress = address;
    }

}

