package main.messageTypes;

import main.Datahandler.*;
/**
 * Peer2PeerMessage
 */
public class Peer2PeerMessage implements PeerMessageType {
	private static int COUNT = 0;

	private DataSegment data;
	private ManageBitFields manageBitFields = null;
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

	public DataSegment getData() {
		return data;
	}

	public ManageBitFields getManageBitFields() {
		return manageBitFields;
	}

	public void setBitFieldHandler(ManageBitFields manageBitFields) {
		this.manageBitFields = manageBitFields;
	}

	public void setData(DataSegment data) {
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
