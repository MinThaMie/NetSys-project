package com.nedap.university.communication;

import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;
import com.nedap.university.utils.Statics;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is a thread that can be used to receive packets
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class Receiver extends Thread{

    private boolean isReceiving = true;
    private Client client;
    private Pi pi;
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<DatagramPacket> queue;
    private int lastFrameReceived = -1;
    private ConcurrentLinkedQueue<Integer> receivedFrames;

    //TODO: create super
    Receiver(Client client){
        this.client = client;
        this.pi = null;
        this.socket = client.getSocket();
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedFrames = new ConcurrentLinkedQueue<>();
    }

    Receiver(Pi pi){
        this.pi = pi;
        this.client = null;
        this.socket = pi.getCommunicationSocket();
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedFrames = new ConcurrentLinkedQueue<>();
    }

    public void run(){
        Listener listener = new Listener();
        listener.start();
    }

    class Listener extends Thread{
        Listener(){
        }

        public void run(){
            while(isReceiving) {
                DatagramPacket received = receiveDatagramPacket();
                if (isPacketValidToReceive(received)) { //Check if the received packet is inside the receiver window, if not, the packet is not presented to the client/pi
                    queue.add(received);
                    setReceivedFrame(Packet.bytesToPacket(received.getData()));
                    if (queue.size() > 0) {
                        if (getClient() != null) {
                            client.packetAvailable(true);
                        } else {
                            pi.packetAvailable(true);
                        }
                    }
                } else {
                    System.out.println("packet is invalid");
                    (Packet.bytesToPacket(received.getData())).print();
                }
            }
        }
    }


    private DatagramPacket receiveDatagramPacket(){
        byte[] buf = new byte[Statics.PACKETSIZE.getValue()];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(recv);
            return recv;
        } catch (IOException e){
            System.out.println("Error in receiving the packet");
        }
        return recv;
    }


    DatagramPacket getFirstPacket(){
        DatagramPacket result =  queue.remove();
        if (queue.size() == 0){
            if(getClient() != null) {
                client.packetAvailable(false);
            }
            else {
                pi.packetAvailable(false);
            }
        }
        return result;
    }

    private Client getClient(){
        return this.client;
    }

    /**
     * Checks is a packet is in the window and is not corrupt. Only then a packet is offered to the client.
     */
    private boolean isPacketValidToReceive(DatagramPacket received){
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();
        return checkIfPacketInsideReceiverWindow(header) && header.checkChecksum();
    }

    private boolean checkIfPacketInsideReceiverWindow(UDPHeader header){
        if (Flag.isSet(Flag.DNS, header.getFlags())){
            lastFrameReceived = header.getSeqNo();
            return true;
        } else {
            return header.getSeqNo() <= lastFrameReceived + Statics.RECEIVERWINDOW.getValue();
        }
    }

    void setReceivedFrame(Packet packet){
        receivedFrames.add(packet.getHeader().getSeqNo());
        updateLFR();
    }

    private void updateLFR() {
        boolean needsUpdate;
        int oldLFR = lastFrameReceived;
        do {
            needsUpdate = false;
            for (Integer seqNo : receivedFrames) {
                if (seqNo == lastFrameReceived + 1) {
                    lastFrameReceived++;
                    receivedFrames.remove(seqNo);
                    needsUpdate = true;
                }
            }
        } while (needsUpdate);
    }


    void setLastFrameReceived(int seqNo){
        this.lastFrameReceived = seqNo;
    }
}
