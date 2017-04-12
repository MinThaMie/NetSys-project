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
    private static int clientPort;
    private static InetAddress clientAddress;
    private static int COMMUNICATION_PORT = 9292;
    static void init(){
        try{
            broadCastSocket = new MulticastSocket(Statics.BROADCASTPORT.value);
        }catch(IOException e){
            e.getMessage();
        }
    }


    static void runPi(){
        while(!dnsIsSet) {
            try {
                byte[] buf = new byte[1000]; //TODO: think of a smart way to trim the data because it appends zeros now
                DatagramPacket received = new DatagramPacket(buf, buf.length);
                broadCastSocket.receive(received);
                clientAddress = received.getAddress();
                Packet receivedPacket = Packet.bytesToPacket(received.getData());
                int flags = receivedPacket.getHeader().getFlags();
                if (Flag.isSet(Flag.DNS, flags)) {
                    sendDNSReply(receivedPacket, clientAddress);
                }
            } catch (IOException e) {
                System.out.println("Error");
            }
        }

        DatagramPacket received = receiveDatagramPacket();
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();
        int flags = receivedPacket.getHeader().getFlags();
        if (Flag.isSet(Flag.FILES, flags)) {
            System.out.println("received file");
            System.out.println(Arrays.toString(receivedPacket.getData()));
            Utils.setFileContents(receivedPacket.getData());
        } else if (Flag.isSet(Flag.ACK, flags)){
            int[] seqAndAck = updateSeqAndAck(getSeqAndAck(header));
            sendSimpleReply(seqAndAck);
        }
    }

    private static void sendDNSReply(Packet receivedPacket, InetAddress address){

        clientPort = receivedPacket.getHeader().getSourceport();
        System.out.println("Received DNS request and reply");
        Packet myPacket = new Packet(COMMUNICATION_PORT, clientPort, new Flag[]{Flag.DNS, Flag.ACK}, 0, 0, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            DatagramPacket replyDNSPacket = new DatagramPacket(myBytes, myBytes.length, address, clientPort);
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

    private static void sendSimpleReply(int[] seqAndAck){
        Packet myPacket = new Packet(COMMUNICATION_PORT,clientPort, new Flag[]{Flag.ACK}, seqAndAck[0], seqAndAck[1], new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            personalSocket.send(new DatagramPacket(myBytes, myBytes.length, clientAddress, clientPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    private static DatagramPacket receiveDatagramPacket(){
        System.out.println("ready to receive some data...");
        byte[] buf = new byte[1000];//TODO: think of a smart way to trim the data because it appends zeros now
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

    private static int[] getSeqAndAck(UDPHeader header){
        int[] result = new int[2];
        result[0] = header.getSeqNo(); //get seqNo
        result[1] = header.getAckNo(); //get ackNo
        return result;
    }

    private static int[] updateSeqAndAck(int[] array){
        int[] result = new int[2];
        result[0] = array[1];
        result[1] = array[0] + 1;
        return result;
    }
}
