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
    private static boolean dnsResolved = false;
    private static int myPort = 7272; //Needs to be fixed because mySocket.getPort only works once it's connected
    private static InetAddress PiAddress;
    private static int PiPort;
    private static boolean isConnected = true;
    private static DatagramSocket mySocket;
    private static volatile boolean packetArrived = false; //TODO: find out what volatile really means?
    private static Receiver myReceiver;
    private static Sender mySender;

    private Client(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this);
    }

    static void init(){
        try {
            mySocket = new DatagramSocket(myPort);
            Client client = new Client();
            client.start();
            myReceiver.start();
            mySender.init();
            mySender.sendDNSPacket(); //You can always do this because you only need the broadcast info;
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        }

        while(isConnected){
            String input = handleTerminalInput();
            if (input.equals("files")){
                System.out.println("Send file request");
                mySender.sendFile();
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
        while(isConnected) {
            if (packetArrived){
                System.out.println("getting packet");
                inspectPacket(myReceiver.getFirstPacket());
            }
        }
        System.out.println("finished");
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

    private static void inspectPacket(DatagramPacket received){
        Packet receivedPacket = Packet.bytesToPacket(received.getData());
        UDPHeader header = receivedPacket.getHeader();
        if(Flag.isSet(Flag.DNS,header.getFlags())){
            PiAddress = received.getAddress();
            mySender.setPiAddress(PiAddress);
            PiPort = received.getPort();
            mySender.setPiPort(PiPort);

        }

        if (Flag.isSet(Flag.ACK, header.getFlags()) && PiAddress != null) {
            int[] seqAndAck = getSeqAndAck(header);
            //mySender.sendSimpleReply(seqAndAck); //TODO: Should be passed to the client who tells the sender
        }
        receivedPacket.print();
    }

    void packetAvailable(boolean bool){
        packetArrived = bool;
    }

    private static int[] getSeqAndAck(UDPHeader header){
        int[] result = new int[2];
        result[0] = header.getSeqNo(); //get seqNo
        result[1] = header.getAckNo(); //get ackNo
        return result;
    }

    DatagramSocket getSocket(){
        return mySocket;
    }
    private static void shutDown(){
        isConnected = false;
    }
}
