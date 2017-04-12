package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
public class Receiver extends Thread{

    private boolean isReceiving = true;
    private Client client;
    private static DatagramSocket clientSocket;
    ConcurrentLinkedQueue<DatagramPacket> queue = new ConcurrentLinkedQueue<>();
    Receiver(Client client){
        this.client = client;
    }

    public void run(){
        clientSocket = client.getSocket();
        Listener listener = new Listener();
        listener.start();
    }


    DatagramPacket getFirstPacket(){
        System.out.println("I get the packet");
        DatagramPacket result =  queue.remove();
        if (queue.size() == 0){
            client.packetAvailable(false);
        }
        return result;
    }


    class Listener extends Thread{
        Listener(){
        }

        public void run(){
            while(isReceiving){
                System.out.println("I'm receiving");
                queue.add(receiveDatagramPacket());
                System.out.println("Queue " + queue.toString());
                if(queue.size() > 0){
                    System.out.println("Packet has arrived");
                    client.packetAvailable(true );
                }
            }
        }
    }

    private static DatagramPacket receiveDatagramPacket(){
        System.out.println("ready to receive some data...");
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            clientSocket.receive(recv);
            return recv;
        } catch (IOException e){
            System.out.println("Error in receiving the packet");
        }
        return recv;
    }

}
