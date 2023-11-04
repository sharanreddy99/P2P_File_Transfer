package main.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import main.PeerController;
import main.constants.Constants;
import main.helper.LogHelper;
import main.helper.MessageHelper;
import main.helper.NextRequestHelper;
import main.messageTypes.HandshakeMessage;
import main.messageTypes.PeerMessage;
import main.messageTypes.PeerMessageType;

/**
 * Peer Handler
 */
public class PeerHandler implements Runnable {
	private PeerController controller; // controller
	private ObjectInputStream objectInputStream; // neighbor peer input stream
	private MessageHelper peerMessageSender; // peerMessageSender
	private NextRequestHelper chunkRequester;
	private LogHelper messageLoggerUtil; // log util

	private String peerId; // peer id
	private Socket neighborSocket; // neighbor peer socket

	private boolean isPreviousMessageReceived = true;
	private boolean isChokedByNeighborPeer = false;
	private boolean isHandshakeReceived = false;
	private boolean isChunkStarted = false;
	private boolean isHandShakeSent = false;
	private boolean hasChoked = false;

	private long downloadTime = 0;
	private int downloadSize = 0;

	/**
	 * get new instance of PeerHandler
	 * 
	 * @param socket
	 * @param controller
	 * @return
	 */
	synchronized public static PeerHandler getNewInstance(Socket socket, PeerController controller) {
		PeerHandler peerHandler = new PeerHandler();
		peerHandler.neighborSocket = socket;
		peerHandler.controller = controller;
		if (!peerHandler.init(controller)) {
			peerHandler.close();
			peerHandler = null;
		}
		return peerHandler;
	}

	/**
	 * init
	 * 
	 * @param controller
	 * @return
	 */
	synchronized private boolean init(PeerController controller) {
		if (neighborSocket == null) {
			return false;
		}
		// System.out.println(LOGGER_PREFIX+" Initializing PeerHandler");

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

		peerMessageSender = MessageHelper.returnPeerMessageSender(neighborPeerOutputStream);
		if (peerMessageSender == null) {
			close();
			return false;
		}
		new Thread(peerMessageSender).start();

		chunkRequester = NextRequestHelper.getNewInstance(controller, this);
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
				PeerMessageType message = (PeerMessageType) objectInputStream.readObject();
				// System.out.println(LOGGER_PREFIX+": "+peerID+": RUN : Received
				// Message:["+message.getMessageNumber()+"]:
				// "+Const.getMessageName(message.getType()));

				// handler message with different message type
				switch (message.messageType()) {
					case Constants.TYPE_HANDSHAKE_MESSAGE:
						if (message instanceof HandshakeMessage) {
							HandshakeMessage handshakeMessage = (HandshakeMessage) message;
							processHandshakeMessage(handshakeMessage);
						} else {
							// send some invalid data
						}
						break;
					case Constants.TYPE_REQUEST_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processRequestMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_BITFIELD_MESSAGE:
						processBitFieldMessage((PeerMessage) message);
						break;
					case Constants.TYPE_CHOKE_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processChokeMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_HAVE_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processHaveMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_INTERESTED_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processInterestedMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_NOT_INTERESTED_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processNotInterestedMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_PIECE_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processPieceMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_UNCHOKE_MESSAGE: {
						PeerMessage peer2PeerMessage = (PeerMessage) message;
						processUnchockMessage(peer2PeerMessage);
						break;
					}
					case Constants.SHUTDOWN_MESSAGE:
						PeerMessage peer2peerMessage = (PeerMessage) message;
						handleShutdownMessage(peer2peerMessage);
						break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * processUnchockMessage
	 * 
	 * @param unchokeMessage
	 */
	private void processUnchockMessage(PeerMessage unchokeMessage) {
		messageLoggerUtil.logMessage("Peer [" + controller.getPeerId() + "] is unchoked by [" + peerId + "]");
		isChokedByNeighborPeer = false;
		try {
			chunkRequester.addPeerMessageToQueue(unchokeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processPieceMessage
	 * 
	 * @param messge
	 */
	private void processPieceMessage(PeerMessage messge) {
		controller.insertPiece(messge, peerId);
		controller.sendHaveMessage(messge.getIndex(), peerId);
		downloadSize += messge.getData().getDataLength();
		setPreviousMessageReceived(true);
		try {
			chunkRequester.addPeerMessageToQueue(messge);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processChokeMessage
	 * 
	 * @param message
	 */
	private void processChokeMessage(PeerMessage message) {
		messageLoggerUtil.logMessage("Peer [" + controller.getPeerId() + "] is choked by [" + peerId + "]");
		isChokedByNeighborPeer = true;
	}

	/**
	 * processBitFieldMessage
	 * 
	 * @param message
	 */
	private void processBitFieldMessage(PeerMessage message) {
		try {
			chunkRequester.addPeerMessageToQueue(message);
			if (isHandshakeReceived && isHandShakeSent && !isChunkStarted()) {
				new Thread(chunkRequester).start();
				startMeasuringDownloadTime();
				setChunkStarted(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processHandshakeMessage
	 * 
	 * @param message
	 */
	private void processHandshakeMessage(HandshakeMessage message) {
		peerId = message.getPeerId();
		sendBitFieldMessage();
		if (!isHandShakeSent) {
			messageLoggerUtil.logMessage("Handshake Message received and processed correctly.");
			messageLoggerUtil.logMessage("Peer " + controller.getPeerId() + " is connected from Peer " + peerId + ".");
			sendHandshakeMessage();
		}

		isHandshakeReceived = true;
		if (isHandShakeSent && !isChunkStarted()) {
			new Thread(chunkRequester).start();
			startMeasuringDownloadTime();
			setChunkStarted(true);
		}
	}

	/**
	 * processRequestMessage
	 * 
	 * @param message
	 */
	private void processRequestMessage(PeerMessage message) {
		if (!hasChoked) {
			PeerMessage pieceMessage = controller.genPieceMessage(message.getIndex());
			if (pieceMessage != null) {
				try {
					Thread.sleep(2000);
					peerMessageSender.sendMessage(pieceMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * processHaveMessage
	 * 
	 * @param message
	 */
	private void processHaveMessage(PeerMessage message) {
		messageLoggerUtil.logMessage("Peer [" + controller.getPeerId() + "] recieved the 'have' message from [" + peerId
				+ "] for the piece" + message.getIndex());
		try {
			chunkRequester.addPeerMessageToQueue(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processInterestedMessage
	 * 
	 * @param message
	 */
	private void processInterestedMessage(PeerMessage message) {
		messageLoggerUtil
				.logMessage("Peer [" + controller.getPeerId() + "] recieved the 'interested' message from [" + peerId
						+ "]");
	}

	/**
	 * processNotInterestedMessage
	 * 
	 * @param message
	 */
	private void processNotInterestedMessage(PeerMessage message) {
		messageLoggerUtil.logMessage(
				"Peer [" + controller.getPeerId() + "] recieved the 'not interested' message from [" + peerId + "]");
	}

	/**
	 * send HandshakeMessage
	 * 
	 * @return
	 */
	synchronized boolean sendHandshakeMessage() {
		try {
			HandshakeMessage message = new HandshakeMessage();
			message.setID(controller.getPeerId());
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

	/**
	 * send BitFieldMessage
	 */
	synchronized void sendBitFieldMessage() {
		try {
			PeerMessage message = controller.getBitFieldMessage();
			messageLoggerUtil
					.logMessage("Peer [" + controller.getPeerId() + "] : exchanging bitfiled " + message.getIndex());
			peerMessageSender.sendMessage(message);
			Thread.sleep(4000);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * send InterestedMessage
	 * 
	 * @param message
	 */
	public void sendInterestedMessage(PeerMessage message) {
		try {
			if (!isChokedByNeighborPeer) {
				peerMessageSender.sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * is file DownloadComplete
	 * 
	 * @return
	 */
	public boolean isDownloadComplete() {
		if (isChunkStarted()) {
			return chunkRequester.isNeighborPeerDownloadedFile();
		} else {
			return false;
		}
	}

	/**
	 * send NotInterestedMessage
	 * 
	 * @param message
	 */
	public void sendNotInterestedMessage(PeerMessage message) {
		try {
			peerMessageSender.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send RequestMessage
	 * 
	 * @param message
	 */
	public void sendRequestMessage(PeerMessage message) {
		try {
			if (!isChokedByNeighborPeer) {
				peerMessageSender.sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send ChokeMessage
	 * 
	 * @param message
	 */
	public void sendChokeMessage(PeerMessage message) {
		try {
			if (!hasChoked) {
				startMeasuringDownloadTime();
				setChoke(true);
				peerMessageSender.sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send UnchokeMessage
	 * 
	 * @param message
	 */
	public void sendUnchokeMessage(PeerMessage message) {
		try {
			if (hasChoked) {
				startMeasuringDownloadTime();
				setChoke(false);
				peerMessageSender.sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processUnchokeMessage
	 * 
	 * @param message
	 */
	public void processUnchokeMessage(PeerMessage message) {
		try {
			peerMessageSender.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send HaveMessage
	 * 
	 * @param message
	 */
	public void sendHaveMessage(PeerMessage message) {
		try {
			peerMessageSender.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send ShutdownMessage
	 * 
	 * @param message
	 */
	public void sendShutdownMessage(PeerMessage message) {
		try {
			peerMessageSender.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startMeasuringDownloadTime() {
		downloadTime = System.currentTimeMillis();
		downloadSize = 0;
	}

	public double downloadSpeed() {
		long timePeriod = System.currentTimeMillis() - downloadTime;
		if (timePeriod != 0) {
			return ((downloadSize * 1.0) / (timePeriod * 1.0));
		} else {
			return 0;
		}
	}

	public void handleShutdownMessage(PeerMessage message) {
		controller.markFileDownloadComplete(peerId);
	}

	private void setChoke(boolean message) {
		hasChoked = message;
	}

	public boolean isPeerChoked() {
		return hasChoked;
	}

	public String getPeerId() {
		return peerId;
	}

	synchronized public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public boolean isPreviousMessageReceived() {
		return isPreviousMessageReceived;
	}

	public void setPreviousMessageReceived(boolean isPieceMessageForPreviousMessageReceived) {
		this.isPreviousMessageReceived = isPieceMessageForPreviousMessageReceived;
	}

	public boolean isHandshakeReceived() {
		return isHandshakeReceived;
	}

	public synchronized boolean isChunkStarted() {
		return isChunkStarted;
	}

	public synchronized void setChunkStarted(boolean isChunkRequestedStarted) {
		this.isChunkStarted = isChunkRequestedStarted;
	}

}
