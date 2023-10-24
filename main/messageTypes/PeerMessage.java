package main.messageTypes;

import main.Datahandler.*;
public class PeerMessage implements PeerMessageType {
	private static int COUNT = 0;

	private DataSegment data;
	private ManageBitFields manageBitFields = null;
	private int index;
	private int length;

	private int messageType;
	public int messageNumber = 0;

	private PeerMessage() {
		messageNumber = ++COUNT;
	}

	public static PeerMessage create() {
		return new PeerMessage();
	}

	public int messageType() {
		return this.messageType;
	}

	public int length() {
		return this.length;
	}

	public int messageNumber() {
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
