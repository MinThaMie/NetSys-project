package com.nedap.university.utils;

import com.nedap.university.packet.Packet;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper class for setting timeouts. Supplied for convenience.
 *
 * @author Jaco ter Braak & Frans van Dijk, Twente University
 * @version 09-02-2016
 * @updated by Anne-Greeth
 */
public class Timeout implements Runnable {
    private static Map<Date, Map<ITimeoutEventHandler, List<Packet>>> eventHandlers = new HashMap<>();
    private static Map<Packet, AbstractMap.SimpleEntry<Date,ITimeoutEventHandler>> packetToDate = new HashMap<>();
    private static Map<Integer, Packet> SeqToPacket = new HashMap<>();
    private static Thread eventTriggerThread;
    private static boolean started = false;
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * Stop timeout of given tag (acknowledged packet)
     */


    public static void stopTimeOut(Packet packet) {
        Packet packetFromList = getPacketBySeq(packet.getHeader().getSeqNo());
        if(packetToDate.get(packetFromList) != null) {
            Date elapsedMoment = packetToDate.get(packetFromList).getKey();
            ITimeoutEventHandler handler = packetToDate.get(packetFromList).getValue();
            if(elapsedMoment != null && handler != null) {
                if(eventHandlers.get(elapsedMoment) != null && eventHandlers.get(elapsedMoment).get(handler) != null) {
                    eventHandlers.get(elapsedMoment).get(handler).remove(packetFromList);
                    System.out.println("Removed timeout with ackNo " + packet.getHeader().getAckNo() + " or seqNo " + packetFromList.getHeader().getSeqNo() );
                }
            }
        }
    }


    /**
     * The seqNo of the received packet is equal to the ackNo of the send packet
     */
    public static Packet getPacketBySeq(int seqNo){
        return SeqToPacket.get(seqNo);
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
     * Stops the helper thread //TODO: this is never used
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
        eventHandlers.get(elapsedMoment).get(handler).add(packet);//Remove time out of previous tag (if there is any)

        if(packetToDate.containsKey(packet)) {
            stopTimeOut(packet);
        }
        packetToDate.put(packet, new AbstractMap.SimpleEntry<>(elapsedMoment,handler));

        SeqToPacket.put(packet.getHeader().getSeqNo(), packet);
        lock.unlock();
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
                    handlersToInvoke.get(handler).forEach(handler::TimeoutElapsed);
                }
                handlersToInvoke.clear();

                Thread.sleep(1);
            } catch (InterruptedException e) {
                runThread = false;
            }
        }

    }
}
