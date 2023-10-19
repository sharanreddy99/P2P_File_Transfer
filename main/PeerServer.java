package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import main.helper.PeerInfoHelper;
import main.connections.ConnectionManager;
import main.messageTypes.PeerInfo;

/**
 * PeerServer
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
	 * returns the peerServer instance
	 * 
	 * @param peerID
	 * @param controller
	 * @return
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
			HashMap<String, PeerInfo> peerInfoMap = peerConfigReader.getPeerInfoMap();
			PeerInfo serverPeerInfo = peerInfoMap.get(peerID);

			serverSocket = new ServerSocket(serverPeerInfo.getPort());
			int maxConnCount = controller.getMaxNewConnectionsCount();
			for (int i = 0; i < maxConnCount; i++) {
				// Accept upto max connections
				Socket incomingSocketConn = serverSocket.accept();

				// Create a peer handler instnace
				ConnectionManager peerHandler = ConnectionManager.createNewInstance(incomingSocketConn, controller);

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