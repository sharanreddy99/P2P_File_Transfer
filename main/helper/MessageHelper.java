package main.helper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import main.constants.Constants;
import main.messageTypes.PeerMessageType;

/**
 * This class sends the specified message to the specified output stream
 * 
 * @author Sharan Sai Reddy Konda
 */
public class MessageHelper implements Runnable {
	private ObjectOutputStream outputStream = null;
	private BlockingQueue<PeerMessageType> messageQueue;

	/**
	 * initializes the PeerMessageSender object and sends the singleton object
	 * 
	 * @param outputStream - outputStream to which the message needs to be sent to
	 * @return PeerMessageSender Singleton Instance
	 */
	public static MessageHelper returnPeerMessageSender(ObjectOutputStream outStream) {
		MessageHelper peerMessageSender = new MessageHelper();
		peerMessageSender.init(outStream);
		return peerMessageSender;
	}

	/**
	 * Initializes the message queue with the specified output stream for storing
	 * the message requests and sending it to the respective stream.
	 * 
	 * @return null
	 */
	private void init(ObjectOutputStream outStream) {
		messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		this.outputStream = outStream;
	}

	/**
	 * Begins the thread execution
	 * 
	 * @return null
	 */
	public void run() {
		try {
			for (;;) {
				// Fetch the message from the queue
				PeerMessageType message = messageQueue.take();

				// Write the output to the output stream
				outputStream.writeUnshared(message);
				outputStream.flush();
			}
		} catch (InterruptedException e) {
			System.out.println(
					"Exception occured while reading message request from the message queue. Message: "
							+ e.getMessage());
		} catch (IOException e) {
			System.out.println(
					"Exception occured while sending request message to the peer through output stream. Message: "
							+ e.getMessage());
		}
	}

	/**
	 * this function adds the send message request to the blocking queue which is
	 * sent by the thread in the backgrround
	 * 
	 * @param message
	 * @throws InterruptedException
	 */
	public void sendMessage(PeerMessageType message) throws InterruptedException {
		messageQueue.put(message);
	}
}
