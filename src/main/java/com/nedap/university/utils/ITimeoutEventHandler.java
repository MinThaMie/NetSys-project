package com.nedap.university.utils;

import com.nedap.university.packet.Packet;

/**
 * Interface for timeout event handlers
 *
 * @author Jaco ter Braak, Twente University
 * @version 11-01-2014
 */
public interface ITimeoutEventHandler {
    /**
     * Is triggered when the timeout has elapsed
     */
    void TimeoutElapsed(Packet packet);
}
