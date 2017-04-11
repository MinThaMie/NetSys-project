package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Client class with all the functions the client needs.
 * The class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
class Client extends Thread {
    private static boolean dnsSend = false;
    private static DatagramSocket mySocket;
    private static int myPort = 7272; //Needs to be fixed because mySocket.getPort only works once it's connected
    private static InetAddress PiAddress;
    private static int PiPort;
    private static boolean isConnected = true;

    private Client(){
        //TODO: Think of a client implementation
    }

    static void init(){
        try {
            mySocket = new DatagramSocket(myPort);
            Client client = new Client();
            client.start();
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        }

        while(isConnected){
            String input = handleTerminalInput();
            if (input.equals("files")){
                System.out.println("Send file request");
                sendFileRequest();
            }
            if (input.equals("shutdown")){
                shutDown();
            }
        }
    }

    /**
     * Does the datatransfer side of things while the main thread takes care of the user input
     */
    public void run(){
        while (!dnsSend) {
            sendDNSPacket();
            while(PiAddress == null){
                inspectPacket(receiveDatagramPacket());
            }
            dnsSend = true;
        }
    }

    private static void sendDNSPacket(){
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

    private static void sendFileRequest(){
        try {
            Packet myPacket = new Packet(myPort,PiPort, new Flag[]{Flag.FILES}, 0, 0, new byte[]{});
            byte[] myBytes = Packet.getByteRepresentation(myPacket);
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, PiAddress, PiPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    private static void sendSimpleReply(int[] seqAndAck){
        Packet myPacket = new Packet(myPort,PiPort, new Flag[]{Flag.ACK}, seqAndAck[0], seqAndAck[1], new byte[]{});
        byte[] myBytes = Packet.getByteRepresentation(myPacket);
        try {
            mySocket.send(new DatagramPacket(myBytes, myBytes.length, PiAddress, PiPort));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }

    private static DatagramPacket receiveDatagramPacket(){
        System.out.println("ready to receive some data...");
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            mySocket.receive(recv);
            return recv;
        } catch (IOException e){
            System.out.println("Error in receiving the packet");
        }
        return recv;
    }

    private static void inspectPacket(DatagramPacket received){
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();
        if(Flag.isSet(Flag.DNS,header.getFlags())){
            System.out.println("DNS " +  header.getSourceport());
            PiAddress = received.getAddress();
            PiPort = received.getPort();
        }

        if (Flag.isSet(Flag.ACK, header.getFlags())){
            System.out.println("ACK " +  header.getSourceport());
            int[] seqAndAck = updateSeqAndAck(getSeqAndAck(header));
            sendSimpleReply(seqAndAck);
        }
        receivedPacket.print();
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


    private static String handleTerminalInput() {
        String msg = "";
        try {
            BufferedReader terminalIn = new BufferedReader(new InputStreamReader(
                    System.in));
            if ((msg = terminalIn.readLine()) != null) { //TODO: check if if is correct or should be while
                System.out.println("typed in terminal" + msg);
                return msg;
            }
        } catch (IOException e) {
            System.out.println("Could not read from buffer");
        }
        return msg;
    }

    private static void shutDown(){
        isConnected = false;
    }
}
