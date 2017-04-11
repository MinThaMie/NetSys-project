package com.nedap.university;

import java.io.*;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private Main() {}

    public static void main(String[] args) { //TODO: remove this
        running = true;
        System.out.println("Hello, Nedap University!");
        String whatAmI = args[0];
        boolean isPi = whatAmI.equals("raspberry");

        if(isPi){
            Pi.init();
        }

        while (keepAlive) {
            try {
                if(isPi){
                    Pi.runPi();
                } else {
                    Client.runClient();
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

}
