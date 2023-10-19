package main.connections;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import main.PeerController;
import main.constants.Constants;
import main.helper.LogHelper;
import main.messageTypes.*;

/**
 * Peer Handler
 */
public class ConnectionManager implements Runnable {
	private PeerController controller; // controller
	private ObjectInputStream objectInputStream; // neighbor peer input stream
	private PeerMessageSender peerMessageSender; // peerMessageSender
	private LogHelper messageLoggerUtil; // log util

	private String peerId; // peer id
	private Socket neighborSocket; // neighbor peer socket
	private boolean isHandshakeReceived = false;
	private boolean isChunkStarted = false;
	private boolean isHandShakeSent = false;
	/**
	 * get new instance of ConnectionManager
	 * 
	 * @param socket
	 * @param controller
	 * @return
	 */
	// Required change also
	synchronized public static ConnectionManager createNewInstance(Socket socket, PeerController controller) {
		ConnectionManager connectionManager = new ConnectionManager();
		connectionManager.neighborSocket = socket;
		connectionManager.controller = controller;
		if (!connectionManager.init(controller)) {
			connectionManager.close();
			connectionManager = null;
		}
		return connectionManager;
	}

	/**
	 * init
	 * 
	 * @param controller
	 * @return
	 */
	// Required change also
	synchronized private boolean init(PeerController controller) {
		if (neighborSocket == null) {
			return false;
		}

		ObjectOutputStream neighborPeerOutputStream;
		try {
			neighborPeerOutputStream = new ObjectOutputStream(neighborSocket.getOutputStream());
			objectInputStream = new ObjectInputStream(neighborSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if (controller == null) {
			close();
			return false;
		}

		peerMessageSender = PeerMessageSender.getNewInstance(neighborPeerOutputStream);
		if (peerMessageSender == null) {
			close();
			return false;
		}
		new Thread(peerMessageSender).start();

		// chunkRequester = ChunkRequester.getNewInstance(controller, this);
		messageLoggerUtil = controller.getLogger();
		return true;
	}

	/**
	 * close
	 */
	synchronized public void close() {
		try {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		} catch (IOException ignore) {
		}
	}

	/**
	 * run handler
	 */
	// Required Change this also
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_SIZE);
		byte[] rawData = new byte[Constants.RAW_DATA_SIZE];

		// as soon as peer enters into thread it will first send handshake message and
		// receive bitfield message
		if (peerId != null) {
			sendHandshakeMessage();
		}
		try {
			// System.out.println(LOGGER_PREFIX+": "+peerID+" : Handshake Message sent");
			while (!controller.isOperationFinish()) {
				// System.out.println(LOGGER_PREFIX+": "+peerID+" : Waiting for connection in
				// while(controller.isOperationCompelete() == false){");
				if (controller.isOperationFinish()) {
					// System.out.println(LOGGER_PREFIX+": "+peerID+": Breaking from while loop");
					break;
				}
				PeerMessage message = (PeerMessage) objectInputStream.readObject();
				// System.out.println(LOGGER_PREFIX+": "+peerID+": RUN : Received
				// Message:["+message.getMessageNumber()+"]:
				// "+Const.getMessageName(message.getType()));

				// handler message with different message type
				switch (message.getType()) {
					case Constants.TYPE_HANDSHAKE_MESSAGE:
						if (message instanceof HandshakeMessage) {
							HandshakeMessage handshakeMessage = (HandshakeMessage) message;
							processHandshakeMessage(handshakeMessage);
						}
						break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// e.printStackTrace();
		}
	}
	/**
	 * processHandshakeMessage
	 * 
	 * @param message
	 */
	private void processHandshakeMessage(HandshakeMessage message) {
		peerId = message.getPeerId();
		//sendBitFieldMessage();
		if (!isHandShakeSent) {
			messageLoggerUtil.logMessage("Handshake Message received and processed correctly.");
			messageLoggerUtil.logMessage("Peer " + controller.getPeerId() + " is connected from Peer " + peerId + ".");
			sendHandshakeMessage();
		}

		isHandshakeReceived = true;
		if (isHandShakeSent && !isChunkStarted()) {
		}
	}

	/**
	 * send HandshakeMessage
	 * 
	 * @return
	 */
	// Required
	synchronized boolean sendHandshakeMessage() {
		try {
			HandshakeMessage message = new HandshakeMessage();
			message.setPeerId(controller.getPeerId());
			peerMessageSender.sendMessage(message);
			isHandShakeSent = true;
			messageLoggerUtil.logMessage("Peer [" + controller.getPeerId() + "] : Handshake Message (P2PFILESHARINGPROJ"
					+ controller.getPeerId() + ") sent");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	public String getPeerId() {
		return peerId;
	}

	synchronized public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public boolean isHandshakeReceived() {
		return isHandshakeReceived;
	}

	public synchronized boolean isChunkStarted() {
		return isChunkStarted;
	}

}
