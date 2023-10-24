package main.connections;

import java.io.*;
import java.net.Socket;

import main.PeerController;
import main.constants.*;
import main.helper.*;
import main.messageTypes.*;
public class ConnectionManager implements Runnable {
	private PeerController peerController;
	private LogHelper logHelper;
	private ObjectInputStream objectInputStream;
	private CommunicateWithPeer communicateWithPeer;
	private boolean hasHandShakeMessageSent;

	private String peerId;
	private Socket peerSocket;

	ConnectionManager(){
		hasHandShakeMessageSent = false;
	}
	
	public String getPeerId() {
		return peerId;
	}

	public Socket getPeerSocket() {
		return peerSocket;
	}

	synchronized public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public static void assignPeerSocketAndController(Socket socket, ConnectionManager connectionManager,  PeerController peerController){
		connectionManager.peerSocket = socket;
		connectionManager.peerController = peerController;
	}
	
	synchronized public static ConnectionManager createNewInstance(Socket socket, PeerController peerController) {
		ConnectionManager connectionManager = new ConnectionManager();
		assignPeerSocketAndController(socket, connectionManager, peerController);
		if(connectionManager.peerSocket == null || !connectionManager.createStreamAndLogger(peerController)){
			connectionManager.closeStreams();
			return connectionManager = null;
		}
		return connectionManager;
	}

	private ObjectOutputStream initializeStreams() {
		ObjectOutputStream peerObjectOutputStream;
		try {
			peerObjectOutputStream = new ObjectOutputStream(peerSocket.getOutputStream());
			objectInputStream = new ObjectInputStream(peerSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return peerObjectOutputStream;
	} 

	private boolean initializePeerMessageSender(PeerController peerController, ObjectOutputStream peerObjectOutputStream) {
		if (peerController != null) {
			communicateWithPeer = CommunicateWithPeer.createNewInstance(peerObjectOutputStream);
			return communicateWithPeer != null;
		}
		return false;
	}

	synchronized private boolean createStreamAndLogger(PeerController peerController) {
		ObjectOutputStream peerObjectOutputStream = initializeStreams();
		if (peerObjectOutputStream == null) {
			return false;
		}
		if (!initializePeerMessageSender(peerController,peerObjectOutputStream)) {
			closeStreams();
			return false;
		}
		logHelper = peerController.getLogger();
		new Thread(communicateWithPeer).start();
		return true;
	}

	synchronized public void closeStreams() {
		if(objectInputStream == null){
			return;
		}
		try {
			objectInputStream.close();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	public void run() {
		if (peerId != null) {
			sendHandshakeMessage();
		}
	
		while (receiveAndProcessMessage()) {
			// Continue receiving and processing messages
		}
	}

	private boolean receiveAndProcessMessage() {
		try {
			PeerMessageType object = (PeerMessageType) objectInputStream.readObject();
			switch (object.messageType()) {
				case Constants.TYPE_HANDSHAKE_MESSAGE:
					if (object instanceof HandshakeMessage) {
						HandshakeMessage handshakeMessage = (HandshakeMessage) object;
						processHandshakeMessage(handshakeMessage);
					}
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private void processHandshakeMessage(HandshakeMessage message) {
		peerId = message.getPeerId();
		if (!hasHandShakeMessageSent) {
			logReceivedHandShakeMessage();
			sendHandshakeMessage();
		}
		// Handshake has been completed.
	}

	synchronized boolean sendHandshakeMessage() {
		try {
			HandshakeMessage message = new HandshakeMessage();
			message.setID(peerController.getPeerId());
			communicateWithPeer.communicateMessageToPeer(message);
			return logHandShakeMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void logReceivedHandShakeMessage(){
		
		logHelper.logMessage("Handshake Message received and processed correctly.");
		StringBuilder logMessage = new StringBuilder("");
		logMessage.append("Peer ");
		logMessage.append(peerController.getPeerId());
		logMessage.append(" is connected from Peer ");
		logMessage.append(peerId+" .");
		logHelper.logMessage(logMessage.toString());

		
	}

	public boolean logHandShakeMessage(){

		StringBuilder logMessage = new StringBuilder("");
		logMessage.append("Peer [ ");
		logMessage.append(peerController.getPeerId());
		logMessage.append(" ] : Handshake Message (P2PFILESHARINGPROJ");
		logMessage.append(peerController.getPeerId());
		logMessage.append(peerId+") sent");
		logHelper.logMessage(logMessage.toString());
		return hasHandShakeMessageSent = true;
	}

}
