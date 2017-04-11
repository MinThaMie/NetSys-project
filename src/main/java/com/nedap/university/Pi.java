package com.nedap.university;

import java.io.IOException;
import java.net.*;

/**
 * Pi class with all the functions and functionalities for the pi.
 * the class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

class Pi {
    private static MulticastSocket broadCastSocket;

    static void init(){
        try{
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.portNo);
        }catch(IOException e){
            e.getMessage();
        }
    }


    static void runPi(){
        try {
            byte[] buf = new byte[1000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            broadCastSocket.receive(recv);
            Packet receivedPacket = Packet.bytesToPacket(recv.getData());
            if (Flag.isSet(Flag.DNS, receivedPacket.getHeader().flags)){
                sendDNSReply(receivedPacket);
            }
        } catch (IOException e){
            System.out.println("Error");
        }
    }

    private static void sendDNSReply(Packet receivedPacket){
        String PI_ADDRESS = "192.168.40.5";
        int COMMUNICATION_PORT = 9292;

        System.out.println("Received DNS request and reply");
        Packet myPacket = new Packet(PI_ADDRESS,COMMUNICATION_PORT,receivedPacket.getHeader().sourceport, new Flag[]{Flag.DNS}, PI_ADDRESS.getBytes());
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            DatagramPacket replyDNSPacket = new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(receivedPacket.getHeader().sourceAddress), receivedPacket.getHeader().sourceport);
            DatagramSocket personalSocket = new DatagramSocket(COMMUNICATION_PORT);
            personalSocket.send(replyDNSPacket);
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }
}
