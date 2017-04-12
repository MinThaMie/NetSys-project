package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * Created by anne-greeth.vanherwijnen on 12/04/2017.
 */
class Sender extends Thread {

    private static int myPort;
    private Client client;
    private static DatagramSocket mySocket;
    private static int PiPort;
    private static InetAddress PiAddress;

    Sender(Client client){
        this.client = client;
    }

    void init(){
        mySocket = client.getSocket();
        myPort = mySocket.getLocalPort();
    }

    void sendDNSPacket(){
        try {
            Packet myPacket = new Packet(myPort,Statics.BROADCASTPORT.value, new Flag[]{Flag.DNS}, 0, 0, new byte[]{});
            byte[] myBytes = Packet.getByteRepresentation(myPacket);
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(Statics.BROADCASTADDRESS.string), Statics.BROADCASTPORT.value));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendFileRequest(){
        try {
            Packet myPacket = new Packet(myPort, PiPort, new Flag[]{Flag.FILES}, 0, 0, new byte[]{});
            byte[] myBytes = Packet.getByteRepresentation(myPacket);
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, PiAddress, PiPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    void sendSimpleReply(int[] seqAndAck){
        int[] updatedSeqAck = updateSeqAndAck(seqAndAck);
        Packet myPacket = new Packet(myPort,PiPort, new Flag[]{Flag.ACK}, updatedSeqAck[0], updatedSeqAck[1], new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, PiAddress, PiPort));
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

    void setPiPort(int port){
        PiPort = port;
    }

    void setPiAddress(InetAddress address){
        PiAddress = address;
    }
}
