package com.nedap.university;

import java.io.*;
import java.net.*;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;
    private static int BROADCAST_PORT = 8080;
    private static String BROADCAST_ADDRESS = "192.168.40.255"; //.255 because of the subnet
    private static String PI_ADDRESS = "192.168.40.5";
    private static String WHOAREYOU = "Who are you?";


    private Main() {}

    public static void main(String[] args) throws Exception { //TODO: remove this
        running = true;
        System.out.println("Hello, Nedap University!");
        String whatAmI = args[0];
        boolean Pi = whatAmI.equals("raspberry");
        MulticastSocket broadCastSocket = new MulticastSocket(BROADCAST_PORT);
        boolean dnsSend = false;

        while (keepAlive) {
            try {
                if(Pi){
                    //String filePath = args[1];
                    //createFolder(filePath); //Creates a folder where the uploaded files should go and the downloaded files should come from
                    try {
                        byte[] buf = new byte[100];
                        DatagramPacket recv = new DatagramPacket(buf, buf.length);
                        broadCastSocket.receive(recv);
                        String recvMSG = new String( recv.getData());
                        System.out.println("received: " + recvMSG);
                    } catch (IOException e){
                        System.out.println("Error");
                    }
                } else {
                    while (!dnsSend) {
                        sendDNSPacket();
                        dnsSend = true;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        initShutdownHook();

        System.out.println("Stopped");
        running = false;
    }

    private static void createFolder(String folder){
        File file = new File(folder);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        } else {
            System.out.println("Directory " + folder + " already exists");
        }
    }

    private static String[] getFiles(String folder){
        File file = new File(folder);
        return file.list();
    }


    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private static void sendDNSPacket(){
        try {
            DatagramSocket mySocket = new DatagramSocket();
            String msg = WHOAREYOU;
            byte[] byteMsg = msg.getBytes();
            mySocket.send(new DatagramPacket(byteMsg, byteMsg.length, InetAddress.getByName(BROADCAST_ADDRESS), BROADCAST_PORT));
        } catch (UnknownHostException e){
            System.out.println("The host is unknown");
        } catch (SocketException e ) {
            System.out.println("Socket could not be opened");
        } catch (IOException e){
            System.out.println("Something else went wrong");
        }
    }
}
