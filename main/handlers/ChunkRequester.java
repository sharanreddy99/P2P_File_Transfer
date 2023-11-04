package main.handlers;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.PeerController;
import main.constants.Constants;
import main.helper.CommonConfigHelper;
import main.helper.BitFieldHelper;
import main.messageTypes.PeerMessage;

/**
 * This class handles the next sequence of messages that are needed to be
 * triggered based on the input request message
 * 
 */
public class ChunkRequester implements Runnable {
	private BlockingQueue<PeerMessage> messageQueue;
	private PeerHandler peerHandler;
	private PeerController controller;
	private BitFieldHelper neighborPeerBFH;

	/**
	 * This function returns the NextRequestHandler obj
	 * 
	 * @param peerController - the main controller object
	 * @param peerHandler    - the peer handler object
	 * @return
	 */
	public static ChunkRequester getNewInstance(PeerController peerController, PeerHandler peerHandler) {
		if (peerHandler == null || peerController == null) {
			return null;
		}

		ChunkRequester requestSender = new ChunkRequester();
		requestSender.setupNeighboringBitFieldManager(peerController, peerHandler);
		return requestSender;
	}

	/**
	 * This function sets up the message queue and also the neighboring bitfield
	 * helper
	 * 
	 * @return null
	 */
	private void setupNeighboringBitFieldManager(PeerController peerController, PeerHandler peerHandler) {
		messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		int pieceSize = Integer.parseInt(CommonConfigHelper.getConfig(Constants.PIECE_SIZE_LABEL));
		float pieceSizeFloat = (float) pieceSize;
		int numOfPieces = (int) Math
				.ceil(Float.parseFloat(CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL)) / pieceSizeFloat);

		neighborPeerBFH = new BitFieldHelper(numOfPieces);
		this.peerHandler = peerHandler;
		this.controller = peerController;
	}

	/**
	 * This function begins the thread execution which reads various message
	 * requests sent to the given peer from the queue and processes the requests by
	 * sending new message requests according to the algorithm
	 * 
	 * @return null
	 */
	public void run() {
		try {
			for (;;) {
				PeerMessage message = messageQueue.take();
				switch (message.getMessageType()) {
					case Constants.TYPE_BITFIELD_MESSAGE:
						handleBitFieldRequest(message);
						break;

					case Constants.TYPE_HAVE_MESSAGE:
						handleHaveRequest(message);
						break;

					case Constants.TYPE_PIECE_MESSAGE:
						handlePieceRequest();
						break;

					case Constants.TYPE_UNCHOKE_MESSAGE:
						handleUnchokeRequest();
						break;
					default:
						break;
				}

			}
		} catch (Exception e) {
			System.out.println(
					"Exception occured when processing message requests inside the class. Message: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * handles the bitfield input request
	 * 
	 * @param message - one of the 8 message types
	 */
	public void handleBitFieldRequest(PeerMessage message) {
		neighborPeerBFH = message.getManageBitFields();
		int missingPieceIdx = getMissingPieceRandomIdx();

		// MissingPieceIdx = -1 indicates that the current peer has no missing pieces to
		// download from the neighbors. Hence in this case, we will be sending not
		// interested message and terminating the peering request
		if (missingPieceIdx != -1) {
			sendInterestedMessage(missingPieceIdx);
		} else {
			peerHandler.sendNotInterestedMessage(PeerMessage.create(Constants.TYPE_NOT_INTERESTED_MESSAGE));
		}
	}

	/**
	 * handles the have input request
	 * 
	 * @param message - one of the 8 message types
	 */
	public void handleHaveRequest(PeerMessage message) {
		int pieceIdx = message.getIndex();
		int randomPieceIdx = getMissingPieceRandomIdx();

		try {
			neighborPeerBFH.setValueAtIndex(pieceIdx, true);
			if (isCurrentPeerMissingThePiece(pieceIdx)) {
				if (peerHandler.isPreviousMessageReceived()) {
					peerHandler.setPreviousMessageReceived(false);
					sendInterestedMessage(pieceIdx);
				}
			} else if (randomPieceIdx != -1) {
				if (peerHandler.isPreviousMessageReceived()) {
					peerHandler.setPreviousMessageReceived(false);
					sendInterestedMessage(randomPieceIdx);
				}
			} else {
				peerHandler.sendNotInterestedMessage(PeerMessage.create(Constants.TYPE_NOT_INTERESTED_MESSAGE));
			}
		} catch (Exception e) {
			System.out.println(
					"Exception occured when handling the `have` request.  Message: "
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * handles the piece input request
	 */
	public void handlePieceRequest() {
		try {
			int missingPieceIdx = getMissingPieceRandomIdx();
			if (missingPieceIdx != -1 && peerHandler.isPreviousMessageReceived()) {
				sendInterestedMessage(missingPieceIdx);
			}
		} catch (Exception e) {
			System.out.println(
					"Exception occured when handling the `piece` request. Message: "
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * handles the unchoke input request
	 */
	private void handleUnchokeRequest() {
		try {
			int missingPieceIdx = getMissingPieceRandomIdx();
			peerHandler.setPreviousMessageReceived(false);
			if (missingPieceIdx != -1) {
				sendInterestedMessage(missingPieceIdx);
			}
		} catch (Exception e) {
			System.out.println(
					"Exception occured when handling the `unchoke` request. Message: "
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Triggers the interested message for the neigboring peer
	 * 
	 * @param pieceIdx - specifies the index of the piece
	 */
	private void sendInterestedMessage(int pieceIdx) {
		// Send interested message for the randomly found missing piece
		PeerMessage newMessage = PeerMessage.create(Constants.TYPE_INTERESTED_MESSAGE);
		newMessage.setIndex(pieceIdx);
		peerHandler.sendInterestedMessage(newMessage);

		sendRequestMessage(pieceIdx);
	}

	/**
	 * Triggers the request messages for the neigboring peer
	 * 
	 * @param pieceIdx - specifies the index of the piece
	 */
	private void sendRequestMessage(int pieceIdx) {
		// Request the piece after sending the interested message
		PeerMessage newMessage = PeerMessage.create(Constants.TYPE_REQUEST_MESSAGE);
		newMessage.setIndex(pieceIdx);
		peerHandler.sendRequestMessage(newMessage);
	}

	/**
	 * This function fetches all the missing pieces info for the given peer and
	 * selects a random missing piece to be requested.
	 * 
	 * @return integer which indicates the index of the piece in the input file
	 */
	public int getMissingPieceRandomIdx() {
		BitFieldHelper currentPeerBFH = controller.getBitFieldMessage().getManageBitFields();
		ArrayList<Integer> missingPiecesIdx = new ArrayList<Integer>();

		for (int i = 0; i < neighborPeerBFH.getNumberOfSegments()
				&& missingPiecesIdx.size() < Constants.MAX_PIECES_LIMIT; i++) {
			if (currentPeerBFH.getValueAtIndex(i) == 0 && neighborPeerBFH.getValueAtIndex(i) == 1) {
				missingPiecesIdx.add(i);
			}
		}

		if (missingPiecesIdx.size() == 0) {
			return -1;
		}

		Random random = new Random();
		return missingPiecesIdx.get(random.nextInt(missingPiecesIdx.size()));
	}

	/**
	 * This function checks if the current peer doesn't have the specified peer
	 * which the neighboring peer has
	 * 
	 * @return boolean indicating whether the condition is true or not
	 */
	public boolean isCurrentPeerMissingThePiece(int pieceIdx) {
		BitFieldHelper currentPeerBFH = controller.getBitFieldMessage().getManageBitFields();
		return currentPeerBFH.getValueAtIndex(pieceIdx) == 0 && neighborPeerBFH.getValueAtIndex(pieceIdx) == 1;
	}

	/**
	 * Adds the peer message request to the blocking queue for processing
	 * 
	 * @param message - One of the 8 peer message requests
	 * @throws InterruptedException
	 */
	public void addPeerMessageToQueue(PeerMessage message) throws InterruptedException {
		messageQueue.put(message);
	}

	/**
	 * Checks whether the neighboring peer has downloaded the complete file or not
	 * 
	 * @return boolean - True if the neighboring peer download is successful
	 */
	public boolean isNeighborPeerDownloadedFile() {
		return neighborPeerBFH != null && neighborPeerBFH.checkIfFileIsDownloaded();
	}
}