package main.messageTypes;

import main.helper.BitFieldHelper;

/**
 * PeerMessage class represents various message types exchanged between peers in
 * a peer-to-peer network.
 * 
 * @author Adithya KNG
 */
public class PeerMessage implements PeerMessageType {
    private static int COUNT = 0;

    private Piece data;
    private BitFieldHelper manageBitFields = null;
    private int index;
    private int length;

    private int messageType;
    public int messageNumber = 0;

    /**
     * Private constructor to create a PeerMessage and increment the message number.
     */
    private PeerMessage() {
        messageNumber = ++COUNT;
    }

    private PeerMessage(int messageType) {
        this.messageType = messageType;
    }

    /**
     * Static factory method to create a new PeerMessage.
     *
     * @return A new PeerMessage instance
     */
    public static PeerMessage create() {
        return new PeerMessage();
    }

    /**
     * Static factory method to create a new PeerMessage.
     *
     * @return A new PeerMessage instance
     */
    public static PeerMessage create(int messageType) {
        return new PeerMessage(messageType);
    }

    /**
     * Static factory method to create a new PeerMessage.
     *
     * @return A new PeerMessage instance
     */
    public static PeerMessage create(int messageType, int index, Piece data) {
        PeerMessage peerMessage = new PeerMessage(messageType);
        peerMessage.setData(data);
        peerMessage.setIndex(index);
        return peerMessage;
    }

    /**
     * Static factory method to create a new PeerMessage.
     *
     * @return A new PeerMessage instance
     */
    public static PeerMessage create(int messageType, int index) {
        PeerMessage peerMessage = new PeerMessage(messageType);
        peerMessage.setIndex(index);
        return peerMessage;
    }

    /**
     * Get the message type of the PeerMessage.
     *
     * @return The message type
     */
    public int messageType() {
        return this.messageType;
    }

    /**
     * Get the length of the PeerMessage.
     *
     * @return The length of the message
     */
    public int length() {
        return this.length;
    }

    /**
     * Get the message number assigned to the PeerMessage.
     *
     * @return The message number
     */
    public int messageNumber() {
        return messageNumber;
    }

    /**
     * Get the index associated with the PeerMessage.
     *
     * @return The index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index associated with the PeerMessage.
     *
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the data segment associated with the PeerMessage.
     *
     * @return The data segment
     */
    public Piece getData() {
        return data;
    }

    /**
     * Get the bit field manager associated with the PeerMessage.
     *
     * @return The bit field manager
     */
    public BitFieldHelper getManageBitFields() {
        return manageBitFields;
    }

    /**
     * Set the bit field manager for the PeerMessage.
     *
     * @param manageBitFields The bit field manager to set
     */
    public void setBitFieldHandler(BitFieldHelper manageBitFields) {
        this.manageBitFields = manageBitFields;
    }

    /**
     * Set the data segment for the PeerMessage.
     *
     * @param data The data segment to set
     */
    public void setData(Piece data) {
        this.data = data;
    }

    /**
     * Set the length of the PeerMessage.
     *
     * @param length The length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Get the message type of the PeerMessage.
     *
     * @return The message type
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Set the message type for the PeerMessage.
     *
     * @param messageType The message type to set
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getHeader() {
        return "";
    }

}
