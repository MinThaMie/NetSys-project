package com.nedap.university.communication;

import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;
import com.nedap.university.utils.FilePrep;
import com.nedap.university.utils.TerminalOutput;
import com.nedap.university.utils.Utils;
import com.nedap.university.utils.Timeout;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;

/**
 * Client class with all the functions the client needs.
 * The class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
public class Client extends Thread {
    private static boolean dnsResolved = false;
    private static boolean isConnected = true;
    private static volatile boolean packetArrived = false; //volatile because receiver thread writes this one --> See report for more info //TODO:include in report
    private static DatagramSocket mySocket;
    private static Receiver myReceiver;
    private static Sender mySender;

    private Client(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this);
    }

    public static void init(){
        TerminalOutput.welcomeMSG();
        int myPort = 7272;
        try {
            Timeout.Start();
            mySocket = new DatagramSocket(myPort);
            Client client = new Client();
            client.start();
            myReceiver.start();
            mySender.start();

            mySender.sendDNSRequest(); //You can always do this because you only need the broadcast info;

            while(isConnected){
                String input = handleTerminalInput();
                if (dnsResolved) {
                    interpretTerminalInput(input);
                }
                if (input.equals("shutdown")){
                    shutDown();
                }
            }
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        }
    }

    /**
     * Does the datatransfer side of things while the main thread takes care of the user input
     */
    public void run(){
        while(isConnected) {
            if (packetArrived){
                inspectPacket(myReceiver.getFirstPacket());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

        //mySender.setSeq(header.getSeqNo());
        if (Flag.isSet(Flag.DNS, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())) { //TODO: Is ACK response needed here?
            mySender.setDestAddress(received.getAddress());
            mySender.setDestPort(received.getPort());
            dnsResolved = true;
            //mySender.sendDNSAck();
            TerminalOutput.DNSResolved();
        }
        if (Flag.isSet(Flag.ACK, header.getFlags()) && dnsResolved) {
            System.out.println("received ack " + header.getSeqNo());
            mySender.setReceivedAck(receivedPacket);
            //mySender.sendSimpleReply();
        }

        if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())){
            try {
                String fileString = new String(receivedPacket.getData(), "UTF-8");
                TerminalOutput.showFiles(fileString);
            } catch (UnsupportedEncodingException e){
                System.out.println("UTF-8 is not known");
            }
        }
        //receivedPacket.print();
    }


    void packetAvailable(boolean bool){
        packetArrived = bool;
    }

    DatagramSocket getSocket(){
        return mySocket;
    }

    private static void shutDown(){
        isConnected = false;
    }

    private static void interpretTerminalInput(String allInput){
        String[] inputArgs = Utils.splitString(allInput, " ");
        String input = inputArgs[0];
        if (input.equals("myFiles")) {
            TerminalOutput.showFiles(getFiles());
        } else if (input.equals("piFiles")){
            mySender.sendFileListRequest();
        } else if (input.equals("uploadFile")){
            String[] allMyFiles = getFiles();
            String selectedFile = allMyFiles[Integer.parseInt(inputArgs[1])];
            File fileToSend = new File("files/" + selectedFile);
            mySender.sendFile(fileToSend, Utils.createSha1(fileToSend));
        }
    }

    private static String[] getFiles(){
        String filePath = "files";
        File file = new File(filePath);
        return file.list();
    }

}
