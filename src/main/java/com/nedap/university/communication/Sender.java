package com.nedap.university.communication;

import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.FilePrep;
import com.nedap.university.utils.ITimeoutEventHandler;
import com.nedap.university.utils.Statics;
import com.nedap.university.utils.Utils;
import com.nedap.university.utils.Timeout;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * This class is used to send packets
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
class Sender extends Thread implements ITimeoutEventHandler {

    private static int myPort;
    private DatagramSocket mySocket;
    private static int destPort;
    private static InetAddress destAddress;
    private boolean isSending;
    ConcurrentLinkedQueue<Packet> queue; //TODO: this assumes that there is one UDP connection
    ConcurrentSkipListSet<Integer> receivedAcks; // Is a list that keeps the natural order --> Acks are in order
    private int lastAckReceived = 0;
    private static int slidingWindowSize = 5;
    private static int lastFrameSend = -1 ;
    private volatile int seqNo;
    private volatile int ackNo;
    private byte[] checksum;

    //TODO: create super constructor
    Sender(Client client){
        this.mySocket = client.getSocket();
        this.queue = new ConcurrentLinkedQueue<>(); //TODO: Check if this needs to be concurrent
        this.receivedAcks = new ConcurrentSkipListSet<>();
        this.seqNo = new Random().nextInt(30) + 1; //+1 because this should not be zero
        this.ackNo = 0; //Is zero because there is no ack on the first message
        this.lastAckReceived = seqNo;
    }

    Sender(Pi pi){
        this.mySocket = pi.getCommunicationSocket();
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedAcks = new ConcurrentSkipListSet<>();
    }

    public void run(){
        init();
        System.out.println("LFS " + lastFrameSend + " LAR " + lastAckReceived + " sws " +slidingWindowSize );
        while(isSending) {
            while (lastFrameSend < lastAckReceived + slidingWindowSize) {
                if(queue.size() > 0) {
                    Packet result = queue.remove();
                    sendPacket(result);
                    if (!Flag.isSet(Flag.ACK, result.getHeader().getFlags())) { //no timeout when it's an ack
                        setTimeOutforPacket(result);
                    }
                    lastFrameSend = result.getHeader().getSeqNo();
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
        Packet myPacket = new Packet(myPort, Statics.BROADCASTPORT.getValue(), new Flag[]{Flag.DNS}, this.seqNo, 0, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(Statics.BROADCASTADDRESS.getString()), Statics.BROADCASTPORT.getValue()));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendDNSReply(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, 0, new byte[]{});
        queue.add(myPacket);
        System.out.println("added really to the queue " + queue.size());
    }

    void sendDNSAck(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, 0, new byte[]{});
        queue.add(myPacket);
    }

    void sendFileFin(){
        System.out.println("I'm gonna send the end");
        this.seqNo++; //TODO: create function to increase seqNO
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.FIN}, this.seqNo, 0, this.checksum);
        queue.add(myPacket);
    }

    void sendFileRequest(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, this.seqNo, 0, new byte[]{});
        queue.add(myPacket);
    }

    void sendSimpleReply(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.ACK}, this.seqNo, 0, new byte[]{});
        queue.add(myPacket);
    }

    void sendFile(File file, byte[] checksum){
        LinkedList<byte[]> dataChunks = FilePrep.filePrep(file);
        this.checksum = checksum;
        int dataChunkPointer = 0;
        while(dataChunkPointer < dataChunks.size()) {
            sendFileChuck(dataChunks.get(dataChunkPointer));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("I'm interrupted");;
            }
            dataChunkPointer++;
        }
        sendFileFin();
    }

    private void sendFileChuck(byte[] fileChunk){
        this.seqNo++;
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, this.seqNo, 0, fileChunk);
        queue.add(myPacket);
    }

    public void TimeoutElapsed(Packet packet) {
        sendPacket(packet);
        System.out.println("Resent packet with seqNo: " + packet.getHeader().getSeqNo()); //Does not need to waitForAck, cause it's already waiting
        setTimeOutforPacket(packet);
    }

    private void setTimeOutforPacket(Packet sendPacket) {
        // schedule a timer for 1000 ms into the future, just to show how that works:
        Timeout.SetTimeout(2000, this, sendPacket);
    }

    void setReceivedAck(Packet packet){
        Timeout.stopTimeOut(packet);
        receivedAcks.add(packet.getHeader().getSeqNo());
        updateLAR();
    }

    private void updateLAR() {
        boolean needsUpdate;
        int oldLAR = lastAckReceived;
        do {
            needsUpdate = false;
            for (Integer ackNumber : receivedAcks) {
                if (ackNumber == lastAckReceived + 1) {
                    lastAckReceived++;
                    receivedAcks.remove(ackNumber);
                    needsUpdate = true;
                }
            }
        } while (needsUpdate);

        //System.out.println("Updated LAR to " + LastAckReceived);
        if(lastAckReceived > oldLAR) {
            System.out.println("New sliding window " + (lastAckReceived + 1) + " - " + (lastAckReceived
                    + slidingWindowSize));
        }
    }

    void setDestPort(int port){
        destPort = port;
    }

    void setDestAddress(InetAddress address){
        destAddress = address;
    }

    void setSeqandAck(int[] updatedSeqAndAck){
        this.seqNo = updatedSeqAndAck[0];
        this.ackNo = 0;
        this.lastAckReceived = this.seqNo - 1;

    }

    void setInitialSeqandAck(int seqNo){ //This function is only used by the Pi
        this.seqNo = seqNo;
        this.ackNo = 0;
        this.lastAckReceived = this.seqNo;
    }
}

