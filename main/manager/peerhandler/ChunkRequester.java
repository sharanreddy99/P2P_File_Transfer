package main.manager.peerhandler;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.PeerController;
import main.DataHandler.ManageBitFields;
import main.constants.Constants;
import main.helper.CommonConfigHelper;
import main.messageTypes.PeerMessage;

public class ChunkRequester implements Runnable {
	/* log */
	private static final String LOGGER_PREFIX = ChunkRequester.class.getSimpleName();

	private BlockingQueue<PeerMessage> messageQueue;
	private PeerController controller;
	private PeerHandler peerHandler;
	private ManageBitFields neighborPeerBitFieldManager = null;

	private boolean isShutDown = false;
	int[] pieceIndexArray = new int[1000];

	/**
	 * get new instance of ChunkRequester
	 * 
	 * @param controller
	 * @param peerHandler
	 * @return
	 */
	public static ChunkRequester getNewInstance(PeerController controller, PeerHandler peerHandler) {
		// System.out.println(LOGGER_PREFIX+" Initializing ChunkRequester");

		if (controller == null || peerHandler == null) {
			return null;
		}

		ChunkRequester requestSender = new ChunkRequester();
		if (!requestSender.init()) {
			requestSender.destroy();
			return null;
		}

		requestSender.controller = controller;
		requestSender.peerHandler = peerHandler;

		// System.out.println(LOGGER_PREFIX+" Initialized ChunkRequester successfully");

		return requestSender;
	}

	/**
	 * init ChunkRequester
	 * 
	 * @return
	 */
	private boolean init() {
		messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		int pieceSize = Integer.parseInt(CommonConfigHelper.getConfig("PieceSize"));
		int numOfPieces = (int) Math
				.ceil(Integer.parseInt(CommonConfigHelper.getConfig("FileSize")) / (pieceSize * 1.0));
		neighborPeerBitFieldManager = new ManageBitFields(numOfPieces);

		return true;
	}

	/**
	 * close ChunkRequester
	 */
	public void destroy() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

	/**
	 * run
	 */
	public void run() {
		if (messageQueue == null) {
			throw new IllegalStateException(LOGGER_PREFIX
					+ ": This object is not initialized properly. This might be result of calling deinit() method");
		}

		while (true) {
			if (isShutDown)
				break;

			try {
				PeerMessage message = messageQueue.take();
				// System.out.println(LOGGER_PREFIX+": Received Message:
				// "+Const.getMessageName(message.getMessageType()));

				PeerMessage requestMessage = PeerMessage.create();
				requestMessage.setMessageType(Constants.TYPE_REQUEST_MESSAGE);

				PeerMessage interestedMessage = PeerMessage.create();
				interestedMessage.setMessageType(Constants.TYPE_INTERESTED_MESSAGE);

				if (message.getMessageType() == Constants.TYPE_BITFIELD_MESSAGE) {
					neighborPeerBitFieldManager = message.getManageBitFields();

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						PeerMessage notInterestedMessage = PeerMessage.create();
						notInterestedMessage.setMessageType(Constants.TYPE_NOT_INTERESTED_MESSAGE);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					} else {
						interestedMessage.setIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);

						requestMessage.setIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}
				}

				if (message.getMessageType() == Constants.TYPE_HAVE_MESSAGE) {
					int pieceIndex = message.getIndex();
					try {
						neighborPeerBitFieldManager.setValueAtIndex(pieceIndex, true);
					} catch (Exception e) {
						System.out.println(LOGGER_PREFIX + "[" + peerHandler.getPeerId()
								+ "]: NULL POINTER EXCEPTION for piece Index" + pieceIndex + " ... "
								+ neighborPeerBitFieldManager);
						e.printStackTrace();
					}

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						PeerMessage notInterestedMessage = PeerMessage.create();
						notInterestedMessage.setMessageType(Constants.TYPE_NOT_INTERESTED_MESSAGE);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					} else {
						if (peerHandler.isPreviousMessageReceived()) {
							peerHandler.setPreviousMessageReceived(false);
							interestedMessage.setIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);

							requestMessage.setIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}
					}
				}

				if (message.getMessageType() == Constants.TYPE_PIECE_MESSAGE) {
					// supposed to send request message only after piece for previous request
					// message.
					int missingPieceIndex = getPieceNumberToBeRequested();

					if (missingPieceIndex != -1) {
						if (peerHandler.isPreviousMessageReceived()) {
							peerHandler.setPreviousMessageReceived(false);
							interestedMessage.setIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);

							requestMessage.setIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}
					}
				} else if (message.getMessageType() == Constants.TYPE_UNCHOKE_MESSAGE) {
					// supposed to send request message after receiving unchoke message
					int missingPieceIndex = getPieceNumberToBeRequested();
					peerHandler.setPreviousMessageReceived(false);
					if (missingPieceIndex != -1) {
						interestedMessage.setIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);

						requestMessage.setIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * getPieceNumberToBeRequested
	 * 
	 * @return
	 */
	public int getPieceNumberToBeRequested() {
		ManageBitFields thisPeerBitFiledHandler = controller.getBitFieldMessage().getManageBitFields();
		int count = 0;
		for (int i = 0; i < neighborPeerBitFieldManager.getNumberOfSegments() && count < pieceIndexArray.length; i++) {
			if (thisPeerBitFiledHandler.getValueAtIndex(i) == 1
					|| neighborPeerBitFieldManager.getValueAtIndex(i) == 0) {
				continue;
			}
			pieceIndexArray[count] = i;
			count++;
		}

		if (count == 0) {
			return -1;
		}
		Random random = new Random();
		int index = random.nextInt(count);
		return pieceIndexArray[index];
	}

	/**
	 * add message into queue
	 * 
	 * @param message
	 * @throws InterruptedException
	 */
	public void addMessage(PeerMessage message) throws InterruptedException {
		if (messageQueue == null) {
			throw new IllegalStateException("");
		} else {
			messageQueue.put(message);
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isNeighborPeerDownloadedFile() {
		return neighborPeerBitFieldManager != null && neighborPeerBitFieldManager.checkIfFileIsDownloaded();
	}
}