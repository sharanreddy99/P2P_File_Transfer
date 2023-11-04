package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import main.handlers.PeerHandler;
import main.helper.PeerInfoHelper;
import main.messageTypes.Peer;

/**
 * This class implements the PeerServer class
 * 
 * @author Bhavan Voram
 */
public class PeerServer implements Runnable {
	private static volatile PeerServer instance = null;

	private String peerID;
	private boolean serverStatus = false;

	private PeerInfoHelper peerConfigReader;
	private PeerController controller;
	private ServerSocket serverSocket;

	private PeerServer(String peerID, PeerController controller) {
		this.peerID = peerID;
		this.controller = controller;
	}

	/**
	 * Returns the singleton instance of the Peer Server
	 * 
	 * @param peerID     - peerID of the current peer
	 * @param controller - main controller object that manages all other objects
	 * @return null
	 */
	public static PeerServer returnSingletonInstance(String peerID, PeerController controller) {
		if (instance == null) {
			instance = new PeerServer(peerID, controller);
			if (instance.isPeerConfigAvailable(controller) == false) {
				instance = null;
			}
		}

		return instance;
	}

	/**
	 * checks if the peer config instance is available (which
	 * stores the logMessage(rmation of each peer from the config file)
	 * 
	 * @param controller
	 * @return boolean
	 */
	public boolean isPeerConfigAvailable(PeerController controller) {
		peerConfigReader = PeerInfoHelper.returnSingletonInstance();
		return peerConfigReader != null;
	}

	/**
	 * returns the server status
	 * 
	 * @param controller
	 * @return boolean
	 */
	public synchronized boolean getServerStatus() {
		return this.serverStatus;
	}

	/**
	 * sets the server status
	 * 
	 * @param controller
	 * @return none
	 */
	public synchronized void setServerStatus(boolean serverStatus) {
		this.serverStatus = serverStatus;
	}

	/**
	 * runs the thread which begins the peering process for the current peer
	 */
	public void run() {
		try {
			HashMap<String, Peer> peerInfoMap = peerConfigReader.getPeerMap();
			Peer serverPeerInfo = peerInfoMap.get(peerID);

			serverSocket = new ServerSocket(serverPeerInfo.getPort());
			int maxConnCount = controller.getMaxNewConnectionsCount();
			for (int i = 0; i < maxConnCount; i++) {
				// Accept upto max connections
				Socket incomingSocketConn = serverSocket.accept();

				// Create a peer handler instnace
				PeerHandler peerHandler = PeerHandler.getNewInstance(incomingSocketConn, controller);

				// Register the peer handler with the controller
				controller.addPeerHandler(peerHandler);

				// Start the peer handler
				new Thread(peerHandler).start();
			}

			// Set the peer handler
			setServerStatus(true);

		} catch (IOException e) {
			System.out.printf("Exception occured inside the thread for peer: %s. Message: %s\n", peerID,
					e.getMessage());
			e.printStackTrace();
		}
	}

}