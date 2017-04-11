package com.nedap.university;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Pi class with all the functions and functionalities for the pi.
 * the class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */

class Pi {
    private static MulticastSocket broadCastSocket;
    private static DatagramSocket personalSocket;
    private static boolean dnsIsSet = false;
    static void init(){
        try{
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.portNo);
        }catch(IOException e){
            e.getMessage();
        }
    }


    static void runPi(){
        while(!dnsIsSet) {
            try {
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                broadCastSocket.receive(recv);
                InetAddress senderAddress = recv.getAddress();
                Packet receivedPacket = Packet.bytesToPacket(recv.getData());
                int flags = receivedPacket.getHeader().flags;
                if (Flag.isSet(Flag.DNS, flags)) {
                    sendDNSReply(receivedPacket, senderAddress);
                }
            } catch (IOException e) {
                System.out.println("Error");
            }
        }
        DatagramPacket received = receiveDatagramPacket();
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        int flags = receivedPacket.getHeader().flags;
        if (Flag.isSet(Flag.FILES, flags)) {
            System.out.println("received file request");
            SendFileRequestResponse();
        } else if (Flag.isSet(Flag.ACK, flags)){
            System.out.println("I've got an Ack");
        }
    }

    private static void sendDNSReply(Packet receivedPacket, InetAddress address){
        int COMMUNICATION_PORT = 9292;

        System.out.println("Received DNS request and reply");
        Packet myPacket = new Packet(COMMUNICATION_PORT,receivedPacket.getHeader().sourceport, new Flag[]{Flag.DNS}, 0, 0, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            DatagramPacket replyDNSPacket = new DatagramPacket(myBytes, myBytes.length, address, receivedPacket.getHeader().sourceport);
            personalSocket = new DatagramSocket(COMMUNICATION_PORT);
            personalSocket.send(replyDNSPacket);
            dnsIsSet = true;
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    private static void SendFileRequestResponse(){ //TODO: implement the real deal (sending it in a useful format)
        String[] files = getFiles();
        System.out.println("those files " + Arrays.toString(files));
    }

    private static DatagramPacket receiveDatagramPacket(){
        System.out.println("ready to receive some data...");
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            personalSocket.receive(recv);
            return recv;
        } catch (IOException e){
            System.out.println("Error in receiving the packet");
        }
        return recv;
    }

    private static String[] getFiles(){
        String filePath = "files";
        File file = new File(filePath);
        return file.list();
    }
}
