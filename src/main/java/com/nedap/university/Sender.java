package com.nedap.university;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * This class is used to send packets
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
class Sender {

    private static int myPort;
    private static DatagramSocket mySocket;
    private static int destPort;
    private static InetAddress destAddress;
    private static volatile boolean receivedAck = false;


    Sender(DatagramSocket socket){
        this.mySocket = socket;
    }

    void init(){
        myPort = mySocket.getLocalPort(); //TODO: getLocalport does not work for the pi for some reason
    }

    void sendDNSRequest(){
        Packet myPacket = new Packet(myPort,Statics.BROADCASTPORT.value, new Flag[]{Flag.DNS}, 0, 0, new byte[]{});
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
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, 0, 0, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        DatagramPacket replyDNSPacket = new DatagramPacket(myBytes, myBytes.length, destAddress, destPort);
        try {
            mySocket.send(replyDNSPacket);
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendDNSAck(){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.DNS, Flag.ACK}, 0, 0, new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        DatagramPacket DNSAckPacket = new DatagramPacket(myBytes, myBytes.length, destAddress, destPort);
        try {
            mySocket.send(DNSAckPacket);
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendFileFin(byte[] checksum){
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES, Flag.FIN}, 0, 0, checksum);
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        DatagramPacket DNSAckPacket = new DatagramPacket(myBytes, myBytes.length, destAddress, destPort);
        try {
            mySocket.send(DNSAckPacket);
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendFileRequest(){
        try {
            Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, 0, 0, new byte[]{});
            byte[] myBytes = Packet.getByteRepresentation(myPacket);
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, destAddress, destPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendSimpleReply(int[] seqAndAck){
        int[] updatedSeqAck = updateSeqAndAck(seqAndAck);
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.ACK}, updatedSeqAck[0], updatedSeqAck[1], new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, destAddress, destPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
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
        Packet myPacket = new Packet(myPort, destPort, new Flag[]{Flag.FILES}, 0, 0, fileChunk);
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try{
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, destAddress, destPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }

    }

    static int[] updateSeqAndAck(int[] array){
        int[] result = new int[2];
        result[0] = array[1];
        result[1] = array[0] + 1;
        return result;
    }

    void setDestPort(int port){
        destPort = port;
    }

    void setDestAddress(InetAddress address){
        destAddress = address;
    }

}

