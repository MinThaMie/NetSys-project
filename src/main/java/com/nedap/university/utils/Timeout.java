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
    private static Map<Integer, Packet> AckToPacket = new HashMap<>();
    private static Thread eventTriggerThread;
    private static boolean started = false;
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * Stop timeout of given tag (acknowledged packet)
     */
    public static void stopTimeoutReceivedPacket(Packet receivedPacket){
        Packet packet = getPacketBySeq(receivedPacket.getHeader().getSeqNo());
        stopTimeOut(packet);
    }

    public static void stopTimeOutOnResend(Packet resendPacket){
        Packet packet = getPacketByAck(resendPacket.getHeader().getAckNo());
        stopTimeOut(packet);
    }

    public static void stopTimeOut(Packet packet) {
        if(packet != null) {
            if(packetToDate.get(packet) != null) {
                Date elapsedMoment = packetToDate.get(packet).getKey();
                ITimeoutEventHandler handler = packetToDate.get(packet).getValue();
                if(elapsedMoment != null && handler != null) {
                    if(eventHandlers.get(elapsedMoment) != null && eventHandlers.get(elapsedMoment).get(handler) != null) {
                        eventHandlers.get(elapsedMoment).get(handler).remove(packet);
                        System.out.println("Removed timeout with ackNo " + packet.getHeader().getAckNo() + " or seqNo " + packet.getHeader().getSeqNo() );
                    }
                }
            }
        }
    }


    /**
     * The seqNo of the received packet is equal to the ackNo of the send packet
     */
    public static Packet getPacketBySeq(int seqNo){
        return AckToPacket.get(seqNo);
    }


    public static Packet getPacketByAck(int ackNo) { return AckToPacket.get(ackNo); }
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

        if(packetToDate.containsKey(getPacketByAck(packet.getHeader().getAckNo()))) {
            stopTimeOut(getPacketByAck(packet.getHeader().getAckNo()));
        }
        packetToDate.put(packet, new AbstractMap.SimpleEntry<>(elapsedMoment,handler));
        if(packetToDate.containsKey(packet)) {
            stopTimeOutOnResend(packet);
        }
        AckToPacket.put(packet.getHeader().getAckNo(), packet);
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
