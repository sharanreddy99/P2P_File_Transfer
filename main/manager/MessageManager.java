package main.manager;

import java.nio.ByteBuffer;

import main.constants.Constants;
import main.messageTypes.HandshakeMessage;
import main.messageTypes.PeerMessage;

/**
 * MessageManager
 */
public class MessageManager {
	private static volatile MessageManager instance;

	/**
	 * get instance
	 * 
	 * @return
	 */
	public static MessageManager returnSingletonInstance() {
		if (instance == null) {
			instance = new MessageManager();
		}
		return instance;
	}

	public byte[] geHandshakeMessage(byte[] rawData) {
		String head = Constants.HANDSHAKE_HEADER_STRING;
		char[] array = head.toCharArray();
		byte[] messageByte = new byte[32];
		for (int i = 0; i < 18; i++) {
			messageByte[i] = (byte) array[i];
		}
		for (int i = 18; i < 31; i++) {
			messageByte[i] = (byte) 0;
		}
		messageByte[31] = rawData[3];

		return messageByte;
	}

	public byte[] getRequestMessage(int pieceIndex) {
		return null;
	}

	public byte[] getChokeMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(Constants.SIZE_OF_EMPTY_MESSAGE);
		buffer.put(Constants.TYPE_CHOKE_MESSAGE);
		return buffer.array();

	}

	public byte[] getUnchokeMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(Constants.SIZE_OF_EMPTY_MESSAGE);
		buffer.put(Constants.TYPE_UNCHOKE_MESSAGE);
		return buffer.array();
	}

	public byte[] getInterestedMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(Constants.SIZE_OF_EMPTY_MESSAGE);
		buffer.put(Constants.TYPE_INTERESTED_MESSAGE);
		return buffer.array();
	}

	public byte[] getNotInterestedMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(Constants.SIZE_OF_EMPTY_MESSAGE);
		buffer.put(Constants.TYPE_NOT_INTERESTED_MESSAGE);
		return buffer.array();

	}

	public byte[] getHaveMessage(byte[] payLoad) {
		ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.putInt(5);
		buffer.put(Constants.TYPE_HAVE_MESSAGE);
		buffer.put(payLoad);
		return buffer.array();

	}

	public byte[] getBitFieldMessage(byte[] byteData) {
		int payloadSize = byteData.length;
		ByteBuffer buffer = ByteBuffer.allocate(payloadSize + 5);
		buffer.putInt(payloadSize + 1);
		buffer.put(Constants.TYPE_BITFIELD_MESSAGE);
		buffer.put(byteData);

		return buffer.array();
	}

	public byte[] getRequestMessage(byte[] payLoad) {
		ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.putInt(5);
		buffer.put(Constants.TYPE_REQUEST_MESSAGE);
		buffer.put(payLoad);
		return buffer.array();
	}

	public HandshakeMessage parseHandShakeMessage(byte[] rawData) {
		return null;
	}

	public PeerMessage parsePeerMessage(byte[] rawData) {
		return null;
	}

	/**
	 * parse bytes to message
	 * 
	 * @param rawData
	 * @return
	 */
	public PeerMessage parse(byte[] rawData) {
		if (rawData == null || rawData.length < 5) {
			return null;
		}

		byte type = rawData[4];
		switch (type) {
			case Constants.TYPE_CHOKE_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setMessageType(Constants.TYPE_CHOKE_MESSAGE);
				message.setLength(1);
				message.setData(null);
				return message;
			}
			case Constants.TYPE_UNCHOKE_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setMessageType(Constants.TYPE_UNCHOKE_MESSAGE);
				message.setLength(1);
				message.setData(null);
				return message;
			}
			case Constants.TYPE_INTERESTED_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setMessageType(Constants.TYPE_INTERESTED_MESSAGE);
				message.setLength(1);
				message.setData(null);
				return message;
			}
			case Constants.TYPE_NOT_INTERESTED_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setMessageType(Constants.TYPE_NOT_INTERESTED_MESSAGE);
				message.setLength(1);
				message.setData(null);
				return message;
			}
			case Constants.TYPE_HAVE_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setLength(5);
				message.setLength(Constants.TYPE_HAVE_MESSAGE);
				message.setIndex(rawData[8]);
				break;
			}
			case Constants.TYPE_REQUEST_MESSAGE: {
				PeerMessage message = PeerMessage.create();
				message.setLength(5);
				message.setLength(Constants.TYPE_REQUEST_MESSAGE);
				message.setIndex(rawData[8]);
				break;
			}
		}
		return null;
	}
}
