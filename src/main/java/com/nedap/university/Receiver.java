package com.nedap.university;

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
    ConcurrentLinkedQueue<DatagramPacket> queue;
    private int lastFrameReceived = -1;
    private int receiverWindowSize = 3;
    Receiver(Client client){
        this.client = client;
        this.pi = null;
        this.socket = client.getSocket();
        this.queue = new ConcurrentLinkedQueue<>();
    }

    Receiver(Pi pi){
        this.pi = pi;
        this.client = null;
        this.socket = pi.getCommunicationSocket();
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void run(){
        Listener listener = new Listener();
        listener.start();
    }

    class Listener extends Thread{
        Listener(){
        }

        public void run(){
            while(isReceiving){
                DatagramPacket received = receiveDatagramPacket();
                if(checkIfPacketInsideReceiverWindow(received)) { //Check if the received packet is inside the receiver window, if not, the packet is not presented to the client/pi
                    queue.add(received);
                    lastFrameReceived = Packet.bytesToPacket(received.getData()).getHeader().getSeqNo();
                    if (queue.size() > 0) {
                        if (getClient() != null) {
                            client.packetAvailable(true);
                        } else {
                            pi.packetAvailable(true);
                        }
                    }
                }
            }
        }
    }

    private DatagramPacket receiveDatagramPacket(){
        byte[] buf = new byte[1000];
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

    private boolean checkIfPacketInsideReceiverWindow(DatagramPacket received){
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();
        return header.getSeqNo() <= lastFrameReceived + receiverWindowSize;
    }
}
