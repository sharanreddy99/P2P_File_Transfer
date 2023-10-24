package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import main.constants.*;
import main.Datahandler.ManageDataSegments;
import main.messageTypes.Peer;
import main.helper.LogHelper;
import main.helper.PeerInfoHelper;
import main.connections.ConnectionManager;

/**
 * Peer Controller
 */
public class PeerController {

	private ArrayList<ConnectionManager> connectionManagers;
	private ManageDataSegments dataSegmentManager;
	private PeerInfoHelper peerInfoHelperObj;
	private PeerServer peerServer;
	private LogHelper logger;
	private String peerId;

	private boolean isConnectionEstablished = false;

	private static PeerController instance = null;

	private PeerController(String peerId){
		this.peerId = peerId;
	}

	/**
	 * returnSingletonInstance: returns a singleton peerController instance for the
	 * given peer
	 */
	public static synchronized PeerController returnSingletonInstance(String peerID) {
		if(instance != null){
			return instance;
		}
		else {
			instance = new PeerController(peerID);
			if (!instance.configControler()) {
				instance = null;
			}
		}
		return instance;
	}

	/**
	 * starts the peer process.
	 */
	public void beginPeerProcess() {
		// start the current peer server
		new Thread(peerServer).start();

		//Now connect to all previously started peers
		HashMap<String, Peer> peerInfoMap = peerInfoHelperObj.getPeerMap();

		try {
			int currentPeerId = Integer.parseInt(peerId);
			for (Map.Entry<String, Peer> set : peerInfoMap.entrySet()) {
				if (Integer.parseInt(set.getKey()) < currentPeerId) {
					establishConnection(peerInfoMap.get(set.getKey()));
				}
			}
		} catch (Exception e) {
			System.out.printf("Exception occured while creating connections with neighbours. Message: %s\n",
					e.getMessage());
			setIsConnectionEstablished(false);
			return;
		}
		setIsConnectionEstablished(true);
	}
	/**
	 * Start a connection with the peer
	 */
	private void establishConnection(Peer peer) throws IOException {
		Socket neighborPeer = new Socket(peer.getAddress(), peer.getPort());
		ConnectionManager getNewPeerHandler = ConnectionManager.createNewInstance(neighborPeer, this);
		setIdAndStartPeer(getNewPeerHandler, peer);
	}

	/*
	 * Add data Segment Manager to the peer controller
	 */
	private void configPieceManager(boolean isFileExists) {
		this.dataSegmentManager = ManageDataSegments.returnSingletonInstance(isFileExists, peerId);
	}

	// Configure piece manager, peer server and attach looger instance
	private boolean configControler() {
		peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance();
		Peer currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
		boolean isFileExists = currPeer != null && currPeer.hasFile();
		this.connectionManagers = new ArrayList<ConnectionManager>();

		// configure piece manager based on whether the peer has the target file or not
		configPieceManager(isFileExists);
		if (dataSegmentManager == null) {
			return false;
		}

		// configure logger instance
		logger = new LogHelper(peerId);
		if (!logger.isLoggerInitialized()) {
			return false;
		}

		// configure peer server
		peerServer = PeerServer.returnSingletonInstance(peerId, this);

		// configuration successful
		return true;
	}

	/**
	 * This function terminates all the necessary objects by closing and freeing
	 * them out from memory and exits the process safely.
	 */
	public void terminateObjects() {
		logger.destroy();
		System.exit(0);
	}

	/**
	 * Add this peer connectionManager to the connectionManagers list
	 */
	public synchronized void addPeerHandler(ConnectionManager connectionManager) {
		connectionManagers.add(connectionManager);
	}
	/**
	 * Return the total new connection count
	 */
	public int getMaxNewConnectionsCount() {
		HashMap<String, Peer> neighborPeerMap = peerInfoHelperObj.getPeerMap();
		List<Integer> peerIdList = neighborPeerMap.keySet()
                .stream()
                .map(key -> {
                    try {
                        return Integer.parseInt(key); // Convert each string key to an integer
                    } catch (NumberFormatException e) {
                        return null; // Handle invalid conversions
                    }
                })
                .filter(Objects::nonNull) // Remove nulls (failed conversions)
                .collect(Collectors.toList());
		int connectionCount = 0;
		int id = Integer.valueOf(peerId);
		for(int i=0; i<peerIdList.size(); i++){
			if(peerIdList.get(i) > id){
				connectionCount++;
			}
		}
		return connectionCount;
	}

	public String getPeerId() {
		return peerId;
	}

	// Retrun true if connection has been established
	public boolean getIsConnectionEstablished() {
		return isConnectionEstablished;
	}

	public void setIsConnectionEstablished(boolean isAllPeersConnection) {
		this.isConnectionEstablished = isAllPeersConnection;
	}

	public synchronized LogHelper getLogger() {
		return logger;
	}

	// Function to set peer id and start the peer
	public void setIdAndStartPeer(ConnectionManager peerHandler, Peer peer) 
	{
		peerHandler.setPeerId(peer.getPeerId());
		connectionManagers.add(peerHandler);
		new Thread(peerHandler).start();
	}
}
