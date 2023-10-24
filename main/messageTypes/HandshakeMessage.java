package main.messageTypes;
import main.constants.*;

public class HandshakeMessage implements PeerMessageType {
	private static int messageNumberCounter = 0;

	private int messageNumber;
	private String ID;

	public HandshakeMessage() {
		attachMessageNumber(this);
	}

	public static void attachMessageNumber(HandshakeMessage obj){
		messageNumberCounter += 1;
		obj.messageNumber = messageNumberCounter;
	}

	public void setID(String peerId) {
		this.ID = peerId;
	}

	public String getPeerId() {
		return ID;
	}

	public int messageType() {
		return Constants.TYPE_HANDSHAKE_MESSAGE;
	}

	public int length() {
		return 0;
	}

	public int messageNumber() {
		return messageNumber;
	}

}
