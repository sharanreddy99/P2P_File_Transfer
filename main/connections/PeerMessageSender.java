package main.connections;

import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.constants.Constants;
import main.messageTypes.PeerMessage;

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
	// Required Change
	public static PeerMessageSender getNewInstance(ObjectOutputStream outputStream) {
		PeerMessageSender peerMessageSender = new PeerMessageSender();
		if (!peerMessageSender.init()) {
			peerMessageSender.destroy();
			return null;
		}

		peerMessageSender.outputStream = outputStream;
		return peerMessageSender;
	}

	// Required
	public void destroy() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

	// Required change
	private boolean init() {
		messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		return true;
	}

	// Required change
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
	// Required
	public void sendMessage(PeerMessage message) throws InterruptedException {
		if (messageQueue != null) {
			messageQueue.put(message);
		} else {
			// throw new IllegalStateException("");
		}
	}

	// Required
	public void shutdown() {
		shutDown = true;
	}
}
