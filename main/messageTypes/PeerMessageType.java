package main.messageTypes;

import java.io.*;

/**
 * The `PeerMessageType` interface represents the common structure for message
 * types exchanged between peers in a peer-to-peer network.
 * 
 * @author Adithya KNG
 */
public interface PeerMessageType extends Serializable {

    /**
     * Get the length of the message.
     *
     * @return The length of the message.
     */
    int length();

    /**
     * Get the message type, which identifies the type of the message.
     *
     * @return The message type.
     */
    int messageType();

    /**
     * Get the message number, which uniquely identifies the message within a
     * session.
     *
     * @return The message number.
     */
    int messageNumber();
}
