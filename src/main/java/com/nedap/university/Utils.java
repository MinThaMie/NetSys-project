package com.nedap.university;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utils class with tiny helpful functions
 * Created by anne-greeth.vanherwijnen on 10/04/2017.
 */
public class Utils {
    static String bytesToHex(byte[] bytes){
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b); //TODO: find out how this works
        }
        return formatter.toString();
    }

    static int bytesToInt(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff); //TODO: Find out how this works
        }
        return (int) value;
    }

    static String stringArrayToString(String[] sArray) {
        return String.join(" ", sArray);
    }

    static String[] splitString(String toBeSplit, String splitBy){
        return toBeSplit.split(splitBy);
    }


    //TODO: PROBABLY NOT NEEDED ANYMORE because i use just + 1 for my seq and acks
    static int[] getSeqAndAck(UDPHeader header){
        int[] result = new int[2];
        result[0] = header.getSeqNo(); //get seqNo
        result[1] = header.getAckNo(); //get ackNo
        return result;
    }

    static int[] updateSeqAndAck(int[] array){
        int[] result = new int[2];
        result[0] = array[1]; //Sequence number is the ack from the previous packet
        result[1] = array[0] + 1; //Ack number is the sequence number + 1
        return result;
    }

    static byte[] setFileContentsPi(byte[] fileContents, int id) {
        File fileToWrite = new File(String.format("/home/pi/files/plaatje%d.jpg", id)); //IS piPath
        byte[] result = new byte[]{};
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e) {
            System.out.println("Could not write the file on the pi");;
        }
        try {
            result = createSha1(fileToWrite);
        } catch (NoSuchAlgorithmException e){
            System.out.println("Your algorithm is not correct");
        } catch (IOException e){
            System.out.println("Could not write sha to stream");
        }
        return result;
    }

    static void setFileContentsClient(byte[] fileContents, int id) {
        File fileToWrite = new File(String.format("plaatje%d.jpg", id)); //is Client path
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (IOException e){
            System.out.println("Could not write the file on the client");
        }
    }

    static byte[] createSha1(File file) throws IOException, NoSuchAlgorithmException{ //TODO: throws is niet zo netjes, maar heeft wel als voordeel dat alles wel bestaat
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fileInput = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fileInput.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }

    static boolean checkChecksum(byte[] checksumReceived, byte[] checksumCalculated){
        return Arrays.equals(checksumReceived, checksumCalculated);
    }


    /**
     * Helper class for setting timeouts. Supplied for convenience.
     *
     * @author Jaco ter Braak & Frans van Dijk, Twente University
     * @version 09-02-2016
     */
    public static class Timeout implements Runnable {
        private static Map<Date, Map<ITimeoutEventHandler, List<Packet>>> eventHandlers = new HashMap<>();
        private static Map<Object, SimpleEntry<Date,ITimeoutEventHandler>> packetToDate = new HashMap<>();
        private static Thread eventTriggerThread;
        private static boolean started = false;
        private static ReentrantLock lock = new ReentrantLock();

        /**
         * Stop timeout of given tag (acknowledged packet)
         * @param packet
         */
        public static void stopTimeOut(Packet packet) {
            if(packet != null) {
                if(packetToDate.get(packet) != null) {
                    Date elapsedMoment = packetToDate.get(packet).getKey();
                    ITimeoutEventHandler handler = packetToDate.get(packet).getValue();
                    if(elapsedMoment != null && handler != null) {
                        if(eventHandlers.get(elapsedMoment) != null && eventHandlers.get(elapsedMoment).get(handler) != null) {
                            eventHandlers.get(elapsedMoment).get(handler).remove(packet);
                            System.out.println("Removed timeout");
                        }
                    }
                }
            }
        }

        /**
         * Starts the helper thread
         */
        public static void Start() {
            if (started)
                throw new IllegalStateException("Already started");
            started = true;
            eventTriggerThread = new Thread(new Timeout());
            eventTriggerThread.start();
        }

        /**
         * Stops the helper thread
         */
        public static void Stop() {
            if (!started)
                throw new IllegalStateException(
                        "Not started or already stopped");
            eventTriggerThread.interrupt();
            try {
                eventTriggerThread.join();
            } catch (InterruptedException e) {
            }
        }

        /**
         * Set a timeout
         *  @param millisecondsTimeout
         *            the timeout interval, starting now
         * @param handler
         * @param packet
         */
        public static void SetTimeout(long millisecondsTimeout,
                                      ITimeoutEventHandler handler, Packet packet) {
            Date elapsedMoment = new Date();
            elapsedMoment
                    .setTime(elapsedMoment.getTime() + millisecondsTimeout);

            lock.lock();

            if (!eventHandlers.containsKey(elapsedMoment)) {
                eventHandlers.put(elapsedMoment,
                        new HashMap<>());
            }
            if (!eventHandlers.get(elapsedMoment).containsKey(handler)) {
                eventHandlers.get(elapsedMoment).put(handler,
                        new ArrayList<>());
            }
            eventHandlers.get(elapsedMoment).get(handler).add(packet);

            lock.unlock();
            System.out.println("Setted time-out");
        }

        /**
         * Do not call this
         */
        @Override
        public void run() {
            boolean runThread = true;
            ArrayList<Date> datesToRemove = new ArrayList<>();
            HashMap<ITimeoutEventHandler, List<Packet>> handlersToInvoke = new HashMap<>();
            Date now;

            while (runThread) {
                try {
                    now = new Date();

                    // If any timeouts have elapsed, trigger their handlers
                    lock.lock();

                    for (Date date : eventHandlers.keySet()) {
                        if (date.before(now)) {
                            System.out.println("should trigger time-out");
                            datesToRemove.add(date);
                            for (ITimeoutEventHandler handler : eventHandlers.get(date).keySet()) {
                                if (!handlersToInvoke.containsKey(handler)) {
                                    handlersToInvoke.put(handler,
                                            new ArrayList<>());
                                }
                                for (Packet packet : eventHandlers.get(date).get(
                                        handler)) {
                                    handlersToInvoke.get(handler).add(packet);
                                }
                            }
                        }
                    }

                    // Remove elapsed events
                    for (Date date : datesToRemove) {
                        eventHandlers.remove(date);
                    }
                    datesToRemove.clear();

                    lock.unlock();

                    // Invoke the event handlers outside of the lock, to prevent
                    // deadlocks
                    for (ITimeoutEventHandler handler : handlersToInvoke
                            .keySet()) {
                        System.out.println("Invoke the timeout");
                        handlersToInvoke.get(handler).forEach(handler::TimeoutElapsed);
                    }
                    handlersToInvoke.clear();

                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println("SHIT CRASHED!!!!"); //TODO: REMOVE!!!!
                    runThread = false;
                }
            }

        }
    }
}
