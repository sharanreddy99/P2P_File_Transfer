package main.manager.peerhandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import main.Controller;
import main.constants.Constants;
import main.helper.MessageLoggerUtil;
import main.messageTypes.HandshakeMessage;
import main.messageTypes.Peer2PeerMessage;
import main.messageTypes.PeerMessage;

/**
 * Peer Handler
 */
public class PeerHandler implements Runnable {
	private Controller controller; // controller
	private ObjectInputStream objectInputStream; // neighbor peer input stream
	private PeerMessageSender peerMessageSender; // peerMessageSender
	private ChunkRequester chunkRequester;
	private MessageLoggerUtil messageLoggerUtil; // log util

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
	synchronized public static PeerHandler getNewInstance(Socket socket, Controller controller) {
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
	synchronized private boolean init(Controller controller) {
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

		peerMessageSender = PeerMessageSender.getNewInstance(neighborPeerOutputStream);
		if (peerMessageSender == null) {
			close();
			return false;
		}
		new Thread(peerMessageSender).start();

		chunkRequester = ChunkRequester.getNewInstance(controller, this);
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
						} else {
							// send some invalid data
						}
						break;
					case Constants.TYPE_REQUEST_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processRequestMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_BITFIELD_MESSAGE:
						processBitFieldMessage((Peer2PeerMessage) message);
						break;
					case Constants.TYPE_CHOKE_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processChokeMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_HAVE_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processHaveMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_INTERESTED_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processInterestedMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_NOT_INTERESTED_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processNotInterestedMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_PIECE_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processPieceMessage(peer2PeerMessage);
						break;
					}
					case Constants.TYPE_UNCHOKE_MESSAGE: {
						Peer2PeerMessage peer2PeerMessage = (Peer2PeerMessage) message;
						processUnchockMessage(peer2PeerMessage);
						break;
					}
					case Constants.SHUTDOWN_MESSAGE:
						Peer2PeerMessage peer2peerMessage = (Peer2PeerMessage) message;
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
	private void processUnchockMessage(Peer2PeerMessage unchokeMessage) {
		messageLoggerUtil.info("Peer [" + controller.getPeerId() + "] is unchoked by [" + peerId + "]");
		isChokedByNeighborPeer = false;
		try {
			chunkRequester.addMessage(unchokeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processPieceMessage
	 * 
	 * @param messge
	 */
	private void processPieceMessage(Peer2PeerMessage messge) {
		controller.insertPiece(messge, peerId);
		controller.sendHaveMessage(messge.getIndex(), peerId);
		downloadSize += messge.getData().getSize();
		setPreviousMessageReceived(true);
		try {
			chunkRequester.addMessage(messge);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processChokeMessage
	 * 
	 * @param message
	 */
	private void processChokeMessage(Peer2PeerMessage message) {
		messageLoggerUtil.info("Peer [" + controller.getPeerId() + "] is choked by [" + peerId + "]");
		isChokedByNeighborPeer = true;
	}

	/**
	 * processBitFieldMessage
	 * 
	 * @param message
	 */
	private void processBitFieldMessage(Peer2PeerMessage message) {
		try {
			chunkRequester.addMessage(message);
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
			messageLoggerUtil.info("Handshake Message received and processed correctly.");
			messageLoggerUtil.info("Peer " + controller.getPeerId() + " is connected from Peer " + peerId + ".");
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
	private void processRequestMessage(Peer2PeerMessage message) {
		if (!hasChoked) {
			Peer2PeerMessage pieceMessage = controller.genPieceMessage(message.getIndex());
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
	private void processHaveMessage(Peer2PeerMessage message) {
		messageLoggerUtil.info("Peer [" + controller.getPeerId() + "] recieved the 'have' message from [" + peerId
				+ "] for the piece" + message.getIndex());
		try {
			chunkRequester.addMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processInterestedMessage
	 * 
	 * @param message
	 */
	private void processInterestedMessage(Peer2PeerMessage message) {
		messageLoggerUtil
				.info("Peer [" + controller.getPeerId() + "] recieved the 'interested' message from [" + peerId + "]");
	}

	/**
	 * processNotInterestedMessage
	 * 
	 * @param message
	 */
	private void processNotInterestedMessage(Peer2PeerMessage message) {
		messageLoggerUtil.info(
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
			message.setPeerId(controller.getPeerId());
			peerMessageSender.sendMessage(message);
			isHandShakeSent = true;
			messageLoggerUtil.info("Peer [" + controller.getPeerId() + "] : Handshake Message (P2PFILESHARINGPROJ"
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
			Peer2PeerMessage message = controller.getBitFieldMessage();
			messageLoggerUtil.info("Peer [" + controller.getPeerId() + "] : exchanging bitfiled " + message.getIndex());
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
	public void sendInterestedMessage(Peer2PeerMessage message) {
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
	public void sendNotInterestedMessage(Peer2PeerMessage message) {
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
	public void sendRequestMessage(Peer2PeerMessage message) {
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
	public void sendChokeMessage(Peer2PeerMessage message) {
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
	public void sendUnchokeMessage(Peer2PeerMessage message) {
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
	public void processUnchokeMessage(Peer2PeerMessage message) {
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
	public void sendHaveMessage(Peer2PeerMessage message) {
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
	public void sendShutdownMessage(Peer2PeerMessage message) {
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

	public void handleShutdownMessage(Peer2PeerMessage message) {
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
