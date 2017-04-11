package com.nedap.university;

import java.io.IOException;
import java.net.*;

/**
 * Client class with all the functions the client needs.
 * The class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
class Client {
    private static boolean dnsSend = false;
    static void runClient(){
        while (!dnsSend) {
            sendDNSPacket();
            dnsSend = true;
        }
        /*byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        broadCastSocket.receive(recv);
        Packet receivedPacket = Packet.bytesToPacket(recv.getData());
        receivedPacket.print();*/ //TODO: needs to be its own socket
    }

    private static void sendDNSPacket(){
        try {
            DatagramSocket mySocket = new DatagramSocket();
            Packet myPacket = new Packet("192.168.10.6",mySocket.getPort(),Statics.BROADCASTPORT.portNo, new Flag[]{Flag.DNS}, new byte[]{});
            byte[] myBytes = Packet.getByteRepresentation(myPacket);
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, InetAddress.getByName(Statics.BROADCASTADDRESS.ipAddress), Statics.BROADCASTPORT.portNo));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }
}
