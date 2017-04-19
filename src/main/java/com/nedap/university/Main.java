package com.nedap.university;

import com.nedap.university.communication.Client;
import com.nedap.university.communication.Pi;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private Main() {}

    public static void main(String[] args) {
        running = true;
        String whatAmI = args[0];
        boolean isPi = whatAmI.equals("raspberry");

        if(isPi){
            Pi.init();
        } else {
            Client.init();
        }

        initShutdownHook();

        System.out.println("Stopped");
        running = false;
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
    } //TODO: Find out what this does

}
