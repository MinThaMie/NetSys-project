package com.nedap.university.communication;

import com.nedap.university.packet.Flag;
import com.nedap.university.packet.Packet;
import com.nedap.university.packet.UDPHeader;
import com.nedap.university.utils.*;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Client class with all the functions the client needs.
 * The class is package private.
 * Created by anne-greeth.vanherwijnen on 11/04/2017.
 */
public class Client extends Thread {
    private static boolean dnsResolved = false;
    private static boolean isConnected = true;
    private static volatile boolean packetArrived = false; //volatile because receiver thread writes this one
    private static DatagramSocket mySocket;
    private static Receiver myReceiver;
    private static Sender mySender;
    private static SortedMap<Integer, byte[]> allByteChunks;
    private static Statistics myStatistics;


    private Client(){
        myReceiver = new Receiver(this);
        mySender = new Sender(this);
        allByteChunks = new TreeMap<>();
        myStatistics = new Statistics();
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
            if ((msg = terminalIn.readLine()) != null) {
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

        if (Flag.isSet(Flag.DNS, header.getFlags()) && Flag.isSet(Flag.ACK, header.getFlags())) {
            mySender.setDestAddress(received.getAddress());
            mySender.setDestPort(received.getPort());
            dnsResolved = true;
            //mySender.sendDNSAck();
            TerminalOutput.DNSResolved();
        }
        if (Flag.isSet(Flag.ACK, header.getFlags()) && dnsResolved) {
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
        if (Flag.isSet(Flag.FILES, header.getFlags()) && !Flag.isSet(Flag.FIN, header.getFlags()) && !Flag.isSet(Flag.ACK, header.getFlags())) {
            receiveFileChunks(receivedPacket.getHeader().getSeqNo(), receivedPacket.getData());
            System.out.println("received packet with seqNo " + receivedPacket.getHeader().getSeqNo());
            mySender.sendSimpleReply(header.getSeqNo());
        }

        if (Flag.isSet(Flag.FILES, header.getFlags()) && Flag.isSet(Flag.FIN, header.getFlags())) {
            System.out.println("I received the end");
            buildReceivedFile(receivedPacket.getData());
            mySender.sendSimpleReply(header.getSeqNo());
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
        Timeout.Stop();
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
        } else if (input.equals("downloadFile")) {
            mySender.sendFileRequest(inputArgs[1].getBytes());
        } else if (input.equals("abort")) {
            System.out.println("You will abort the download");
            mySender.sendAbort();
        } else if (input.equals("help")){
            TerminalOutput.menuMSG();
        } else if (input.equals("shutdown")){
            shutDown();
        } else {
            System.out.println("You've typed an unknown command, please type help if you don't remember the commands.");
        }
    }

    private static String[] getFiles(){
        String filePath = "files";
        File file = new File(filePath);
        return file.list();
    }

    private static void receiveFileChunks(Integer seqNo, byte[] data){
        if (allByteChunks.size() == 0){
            myStatistics.setStartTime(new Date());
        }
        if (!allByteChunks.containsKey(seqNo)) {
            allByteChunks.put(seqNo, data);
        }
    }

    private static void buildReceivedFile(byte[] receveidCheckSum){
        myStatistics.setEndTime(new Date());
        int id = new Random().nextInt(100);
        Utils.setFileContentsClient(allByteChunks, id , "png");
        byte[] calculatedChecksum;
        File receivedfile = new File(String.format("files/plaatje%d.png", id));
        calculatedChecksum = Utils.createSha1(receivedfile);
        myStatistics.calculateSpeed(allByteChunks.size(), receivedfile.length());
        System.out.println("Got checksum from plaatje " + id);
        System.out.println("The receivedChecksum = " + Arrays.toString(receveidCheckSum));
        System.out.println("The calculatedChecksum = " + Arrays.toString(calculatedChecksum));
        System.out.println("The checksums are " + Utils.checkChecksum(receveidCheckSum, calculatedChecksum));
        allByteChunks.clear(); //Clear the map to be ready to receive a new file
    }

}
