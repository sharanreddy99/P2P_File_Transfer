package main.connections;

import java.io.*;
import java.util.concurrent.*;

import main.constants.*;
import main.messageTypes.*;

public class CommunicateWithPeer implements Runnable {

	private ObjectOutputStream objectOutputStream = null;
	private boolean connectionInactive = false;

	private BlockingQueue<PeerMessageType> messageQueue;

	// Function to toggle the connection status (active/inactive)
	public void toggleConnectionInactive() {
		connectionInactive = !connectionInactive;
	}

	/**
	 * Create a new instance of CommunicateWithPeer
	 * 
	 * @param outputStream ObjectOutputStream to send data to the peer
	 * @return New instance of CommunicateWithPeer
	 */
	public static CommunicateWithPeer createNewInstance(ObjectOutputStream outputStream) {
		CommunicateWithPeer communicateWithPeer = new CommunicateWithPeer();
		return !communicateWithPeer.createNewQueue() && communicateWithPeer.removeQueue()
			? null
			: attachOutputStream(outputStream, communicateWithPeer);
	}

	// Function to attach an ObjectOutputStream to the CommunicateWithPeer instance
	public static CommunicateWithPeer attachOutputStream(ObjectOutputStream outputStream, CommunicateWithPeer communicateWithPeer) {
		communicateWithPeer.objectOutputStream = outputStream;
		return communicateWithPeer;
	}

	// Create a new message queue for communication
	private boolean createNewQueue() {
		try {
			messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// Define the thread run function
	public void run() {
		if (connectionInactive)
			return;
		while (messageQueue != null && !connectionInactive) {
			try {
				PeerMessageType head = messageQueue.take();
				objectOutputStream.writeUnshared(head);
				objectOutputStream.flush();
			} catch (Exception e) {
				return;
			}
		}
	}

	/**
	 * Communicate a message to the peer.
	 * 
	 * @param message PeerMessageType to be communicated
	 * @throws Exception if there are communication issues
	 */
	public void communicateMessageToPeer(PeerMessageType message) throws Exception {
		if (!checkForMessageQueue(messageQueue)) {
			return;
		}
		try {
			messageQueue.put(message);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// Check if the message queue is available
	public static boolean checkForMessageQueue(BlockingQueue<PeerMessageType> messageQueue) {
		return messageQueue != null;
	}

	// Remove the message queue
	public boolean removeQueue() {
		try {
			if (messageQueue == null) {
				return true;
			} else if (messageQueue.size() != 0) {
				messageQueue.clear();
				messageQueue = null;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
