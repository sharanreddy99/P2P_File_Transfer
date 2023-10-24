package main.connections;

import java.io.*;
import java.util.concurrent.*;

import main.constants.*;
import main.messageTypes.*;

public class CommunicateWithPeer implements Runnable {
	
	private ObjectOutputStream objectOutputStream = null;
	private boolean connectionInactive = false;

	private BlockingQueue<PeerMessageType> messageQueue;

	public void toggleConnectionInactive() {
		connectionInactive = !connectionInactive;
	}

	/**
	 * get new instance of CommunicateWithPeer
	 * 
	 * @param outputStream
	 * @return
	 */
	public static CommunicateWithPeer createNewInstance(ObjectOutputStream outputStream) {
		CommunicateWithPeer communicateWithPeer = new CommunicateWithPeer();
		return !communicateWithPeer.createNewQueue() && communicateWithPeer.removeQueue() 
			? null 
			: attachOutputStream (outputStream, communicateWithPeer);
	}

	public static CommunicateWithPeer attachOutputStream(ObjectOutputStream outputStream, CommunicateWithPeer communicateWithPeer){
		communicateWithPeer.objectOutputStream = outputStream;
		return communicateWithPeer;
	}
	private boolean createNewQueue() {
		try{
			messageQueue = new ArrayBlockingQueue<>(Constants.SENDER_QUEUE_SIZE);
		}catch(Exception e){
			return false;
		}
		return true;
	}

	// Define the thread run function
	public void run() {
		if (connectionInactive)
			return;
		while(messageQueue != null && !connectionInactive){
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
	 * communicate message to peer
	 * 
	 * @param message
	 * @throws InterruptedException
	 */
	public void communicateMessageToPeer(PeerMessageType message) throws Exception {
		if(!checkForMessageQueue(messageQueue)){
			return;
		}
		try{
			messageQueue.put(message);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	public static boolean checkForMessageQueue(BlockingQueue<PeerMessageType> messageQueue){
		return messageQueue != null;
	}

	public boolean removeQueue() {
		try{
			if(messageQueue == null){
				return true;
			}
			else if(messageQueue.size() != 0) {
				messageQueue.clear();
				messageQueue = null;
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
}
