package main.message;

import main.manager.filehandler.BitFieldHandler;
import main.message.meta.Piece;

/**
 * Peer2PeerMessage
 */
public class Peer2PeerMessage implements PeerMessage {
	private static int COUNT = 0;

	private Piece data;
	private BitFieldHandler bitFieldHandler = null;
	private int index;
	private int length;

	private int messageType;
	public int messageNumber = 0;

	private Peer2PeerMessage() {
		messageNumber = ++COUNT;
	}

	public static Peer2PeerMessage create() {
		return new Peer2PeerMessage();
	}

	public int getType() {
		return this.messageType;
	}

	public int getLength() {
		return this.length;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public byte[] getMessage() {
		return null;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Piece getData() {
		return data;
	}

	public BitFieldHandler getBitFieldHandler() {
		return bitFieldHandler;
	}

	public void setBitFieldHandler(BitFieldHandler bitFieldHandler) {
		this.bitFieldHandler = bitFieldHandler;
	}

	public void setData(Piece data) {
		this.data = data;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

}
