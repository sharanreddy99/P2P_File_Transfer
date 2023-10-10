package main.manager.peerhandler;

import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.constants.Constants;
import main.message.PeerMessage;

public class PeerMessageSender implements Runnable {
	/* log */
	private static final String LOGGER_PREFIX = PeerMessageSender.class.getSimpleName();

	private ObjectOutputStream outputStream = null;
	private BlockingQueue<PeerMessage> messageQueue;
	private boolean shutDown = false;

	/**
	 * get new instance of PeerMessageSender
	 * 
	 * @param outputStream
	 * @return
	 */
	public static PeerMessageSender getNewInstance(ObjectOutputStream outputStream) {
		PeerMessageSender peerMessageSender = new PeerMessageSender();
		if (!peerMessageSender.init()) {
			peerMessageSender.destroy();
			return null;
		}

		peerMessageSender.outputStream = outputStream;
		return peerMessageSender;
	}

	public void destroy() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

	private boolean init() {
		messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		return true;
	}

	public void printMessageDetails(PeerMessage message) {
		// if(message.getType() != Const.TYPE_HAVE_MESSAGE && message.getType() !=
		// Const.TYPE_NOT_INTERESTED_MESSAGE && message.getType() !=
		// Const.TYPE_INTERESTED_MESSAGE){
		// if(message.getType() == Const.TYPE_PIECE_MESSAGE || message.getType() ==
		// Const.TYPE_REQUEST_MESSAGE){
		//// System.out.println(LOGGER_PREFIX+": Sent
		// message:["+message.getMessageNumber()+"]:"+Const.getMessageName(message.getType())
		// +" Piece Number : "+((Peer2PeerMessage)message).getPieceIndex());
		//// System.out.println(LOGGER_PREFIX+":["+handler.getPeerId()+"]"+": Sent
		// message:["+message.getMessageNumber()+"]:"+Const.getMessageName(message.getType())
		// +" Piece Number : "+((Peer2PeerMessage)message).getPieceIndex());
		// }else{
		//// System.out.println(LOGGER_PREFIX+": Sent
		// message:["+message.getMessageNumber()+"]:"+Const.getMessageName(message.getType())
		// );
		//// System.out.println(LOGGER_PREFIX+":["+handler.getPeerId()+"]"+": Sent
		// message:["+message.getMessageNumber()+"]:"+Const.getMessageName(message.getType()));
		// }
		// }
	}

	public void run() {
		if (messageQueue == null) {
			throw new IllegalStateException(LOGGER_PREFIX
					+ ": This object is not initialized properly. This might be result of calling deinit() method");
		}

		while (true) {
			if (shutDown)
				break;
			try {
				PeerMessage message = messageQueue.take();
				outputStream.writeUnshared(message);
				outputStream.flush();
				printMessageDetails(message);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * sendMessage
	 * 
	 * @param message
	 * @throws InterruptedException
	 */
	public void sendMessage(PeerMessage message) throws InterruptedException {
		if (messageQueue != null) {
			messageQueue.put(message);
		} else {
			// throw new IllegalStateException("");
		}
	}

	public void shutdown() {
		shutDown = true;
	}
}
