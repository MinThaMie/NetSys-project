package com.nedap.university.communication;

import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.utils.FilePrep;
import com.nedap.university.utils.ITimeoutEventHandler;
import com.nedap.university.utils.Statics;
import com.nedap.university.utils.Timeout;

import java.io.File;
import java.io.IOException;
import java.net.*;
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
    private ConcurrentLinkedQueue<Packet> queue;
    private ConcurrentSkipListSet<Integer> receivedAcks; // Is a list that keeps the natural order --> Acks are in order
    private int lastAckReceived = 0;
    private static int slidingWindowSize = 15;
    private static int lastFrameSend = -1 ;
    private volatile int seqNo;
    private byte[] checksum;

    Sender(Client client){
        this.mySocket = client.getSocket();
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedAcks = new ConcurrentSkipListSet<>();
        this.seqNo = new Random().nextInt(30) + 1; //+1 because this should not be zero
        this.lastAckReceived = seqNo;
    }

    Sender(Pi pi){
        this.mySocket = pi.getCommunicationSocket();
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedAcks = new ConcurrentSkipListSet<>();
    }

    public void run(){
        init();
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
        myPort = mySocket.getLocalPort();
        isSending = true;
    }

    private void sendPacket(Packet packet){
        byte[] myBytes = packet.getByteRepresentation();
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, destAddress, destPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendDNSRequest(){
        Packet myPacket = new Packet(myPort, Statics.BROADCASTPORT.getValue(), new Flag[]{Flag.DNS}, this.seqNo, new byte[]{});
        byte[] myBytes = myPacket.getByteRepresentation();
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(Statics.BROADCASTADDRESS.getString()), Statics.BROADCASTPORT.getValue()));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something in the DNS request went wrong");
        }
    }

    void sendDNSReply(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, new byte[]{});
        prepPacketAndSetToQueue(myPacket);
    }

    void sendDNSAck(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, this.seqNo, new byte[]{});
        prepPacketAndSetToQueue(myPacket);
    }

    void sendFileFin(){
        System.out.println("I'm gonna send the end");
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.FIN}, this.seqNo, this.checksum);
        prepPacketAndSetToQueue(myPacket);
    }

    void sendFileListRequest(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.SYN}, this.seqNo, new byte[]{});
        prepPacketAndSetToQueue(myPacket);
    }

    void sendFileListReply(byte[] data, int seqNo){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.ACK}, seqNo, data);
        prepPacketAndSetToQueue(myPacket);
    }

    void sendFileRequest(byte[] data){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.REQUEST}, this.seqNo, data);
        prepPacketAndSetToQueue(myPacket);
    }

    void sendSimpleReply(int seqNo){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.ACK}, seqNo, new byte[]{});
        sendPacket(myPacket); //This packet dus not need to be prepped since the seqNo comes directly from the received packet
    }

    void sendFile(File file, byte[] checksum){
        LinkedList<byte[]> dataChunks = FilePrep.filePrep(file);
        this.checksum = checksum;
        int dataChunkPointer = 0;
        while(dataChunkPointer < dataChunks.size()) {
            sendFileChuck(dataChunks.get(dataChunkPointer));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("I'm interrupted");
            }
            dataChunkPointer++;
        }
        sendFileFin();
    }

    private void sendFileChuck(byte[] fileChunk){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, this.seqNo, fileChunk);
        prepPacketAndSetToQueue(myPacket);
    }

    void sendAbort(){
        Packet abortPacket = new Packet(myPort, destPort, new Flag[]{Flag.ABORT}, this.seqNo, new byte[]{});
        sendPacket(abortPacket);
    }

    public void TimeoutElapsed(Packet packet) {
        sendPacket(packet);
        System.out.println("Resent packet with seqNo: " + packet.getHeader().getSeqNo()); //Does not need to waitForAck, cause it's already waiting
        //setTimeOutforPacket(packet); // probably not necessary since the timeout is also set on in the sendLoop
    }

    private void setTimeOutforPacket(Packet sendPacket) {
        // schedule a timer for 1000 ms into the future, just to show how that works:
        Timeout.SetTimeout(10000, this, sendPacket);
    }

    void setReceivedAck(Packet packet){
        Timeout.stopTimeOut(packet);
        receivedAcks.add(packet.getHeader().getSeqNo());
        updateLAR();
    }

    private void updateLAR() {
        boolean needsUpdate;
        //int oldLAR = lastAckReceived;
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
    }

    void setDestPort(int port){
        destPort = port;
    }

    void setDestAddress(InetAddress address){
        destAddress = address;
    }

    void setInitialSeqandAck(int seqNo){ //This function is only used by the Pi
        this.seqNo = seqNo;
        this.lastAckReceived = this.seqNo;
    }

    private void prepPacketAndSetToQueue(Packet packet){
        this.seqNo++;
        packet.setSeqNo(this.seqNo);
        queue.add(packet);
    }

    void abortDownload(){
        System.out.println("size before " + queue.size());
        queue.clear();
        System.out.println("size after " + queue.size());
    }

}

