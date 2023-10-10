package main.manager.peerhandler;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.Controller;
import main.common.Constants;
import main.helper.CommonPropertyUtil;
import main.manager.filehandler.BitFieldHandler;
import main.message.Peer2PeerMessage;

public class ChunkRequester implements Runnable {
	/* log */
	private static final String LOGGER_PREFIX = ChunkRequester.class.getSimpleName();

	private BlockingQueue<Peer2PeerMessage> messageQueue;
	private Controller controller;
	private PeerHandler peerHandler;
	private BitFieldHandler neighborPeerBitFieldhandler = null;

	private boolean isShutDown = false;
	int[] pieceIndexArray = new int[1000];

	/**
	 * get new instance of ChunkRequester
	 * 
	 * @param controller
	 * @param peerHandler
	 * @return
	 */
	public static ChunkRequester getNewInstance(Controller controller, PeerHandler peerHandler) {
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
		int pieceSize = Integer.parseInt(CommonPropertyUtil.getProperty("PieceSize"));
		int numOfPieces = (int) Math
				.ceil(Integer.parseInt(CommonPropertyUtil.getProperty("FileSize")) / (pieceSize * 1.0));
		neighborPeerBitFieldhandler = new BitFieldHandler(numOfPieces);

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
				Peer2PeerMessage message = messageQueue.take();
				// System.out.println(LOGGER_PREFIX+": Received Message:
				// "+Const.getMessageName(message.getType()));

				Peer2PeerMessage requestMessage = Peer2PeerMessage.create();
				requestMessage.setMessageType(Constants.TYPE_REQUEST_MESSAGE);

				Peer2PeerMessage interestedMessage = Peer2PeerMessage.create();
				interestedMessage.setMessageType(Constants.TYPE_INTERESTED_MESSAGE);

				if (message.getType() == Constants.TYPE_BITFIELD_MESSAGE) {
					neighborPeerBitFieldhandler = message.getBitFieldHandler();

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						Peer2PeerMessage notInterestedMessage = Peer2PeerMessage.create();
						notInterestedMessage.setMessageType(Constants.TYPE_NOT_INTERESTED_MESSAGE);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					} else {
						interestedMessage.setIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);

						requestMessage.setIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}
				}

				if (message.getType() == Constants.TYPE_HAVE_MESSAGE) {
					int pieceIndex = message.getIndex();
					try {
						neighborPeerBitFieldhandler.setBitField(pieceIndex, true);
					} catch (Exception e) {
						System.out.println(LOGGER_PREFIX + "[" + peerHandler.getPeerId()
								+ "]: NULL POINTER EXCEPTION for piece Index" + pieceIndex + " ... "
								+ neighborPeerBitFieldhandler);
						e.printStackTrace();
					}

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						Peer2PeerMessage notInterestedMessage = Peer2PeerMessage.create();
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

				if (message.getType() == Constants.TYPE_PIECE_MESSAGE) {
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
				} else if (message.getType() == Constants.TYPE_UNCHOKE_MESSAGE) {
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
		BitFieldHandler thisPeerBitFiledHandler = controller.getBitFieldMessage().getBitFieldHandler();
		int count = 0;
		for (int i = 0; i < neighborPeerBitFieldhandler.getSize() && count < pieceIndexArray.length; i++) {
			if (thisPeerBitFiledHandler.getBitField(i) || !neighborPeerBitFieldhandler.getBitField(i)) {
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
	public void addMessage(Peer2PeerMessage message) throws InterruptedException {
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
		return neighborPeerBitFieldhandler != null && neighborPeerBitFieldhandler.isFileDownloadComplete();
	}
}