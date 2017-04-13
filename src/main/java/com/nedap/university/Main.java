package com.nedap.university;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private Main() {}

    public static void main(String[] args) {
        running = true;
        System.out.println("Hello, Nedap University!");
        String whatAmI = args[0];
        boolean isPi = whatAmI.equals("raspberry");

        if(isPi){
            Pi.init();
        } else {
            Client.init();
        }

        while (keepAlive) {
            try {
                if(isPi){
                   // Pi.runPi();
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
