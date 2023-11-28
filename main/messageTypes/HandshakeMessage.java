package main.messageTypes;

import main.constants.*;

/**
 * This class represents the Handshake Message used during the peer to peer
 * communcation
 * 
 * @author Adithya KNG
 */
public class HandshakeMessage implements PeerMessageType {
    private static int messageNumberCounter = 0;

    private int messageNumber;
    private String header;
    private String ID;

    /**
     * Constructor to create a HandshakeMessage and attach a message number.
     */
    public HandshakeMessage(String header) {
        attachMessageNumber(this);
        this.header = header;
    }
    /**
     * Constructor to create a HandshakeMessage and attach a message number.
     * and it
     */
    public HandshakeMessage(String header, String id) {
        attachMessageNumber(this);
        this.header = header;
        setID(id);
    }

    /**
     * Attach a message number to the HandshakeMessage.
     *
     * @param obj The HandshakeMessage object to which the message number is
     *            attached.
     */
    public static void attachMessageNumber(HandshakeMessage obj) {
        messageNumberCounter += 1;
        obj.messageNumber = messageNumberCounter;
    }

    /**
     * Set the peer ID for the HandshakeMessage.
     *
     * @param peerId The peer ID to set
     */
    public void setID(String peerId) {
        this.ID = peerId;
    }

    /**
     * Get the peer ID from the HandshakeMessage.
     *
     * @return The peer ID
     */
    public String getPeerId() {
        return ID;
    }

    /**
     * Get the message type of the HandshakeMessage.
     *
     * @return The message type as defined in Constants
     */
    public int messageType() {
        return Constants.TYPE_HANDSHAKE_MESSAGE;
    }

    /**
     * Get the length of the HandshakeMessage.
     *
     * @return The length of the message (always 0 for handshake)
     */
    public int length() {
        return 0;
    }

    /**
     * Get the message number assigned to the HandshakeMessage.
     *
     * @return The message number
     */
    public int messageNumber() {
        return messageNumber;
    }

    /**
     * Get the header which is set for the handshake message
     *
     * @return The message header
     */
    public String getHeader() {
        return this.header;
    }
}
