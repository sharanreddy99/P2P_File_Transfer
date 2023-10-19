package main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import main.constants.Constants;
import main.connections.ConnectionManager;
import main.handlers.filehandler.PieceManager;
import main.helper.LogHelper;
import main.helper.PeerInfoHelper;
import main.messageTypes.PeerInfo;

/**
 * Controller
 */
public class PeerController {

	private ArrayList<ConnectionManager> connectionManagers;
	private PieceManager pieceManager;
	private PeerInfoHelper peerInfoHelperObj;
	private PeerServer peerServer;
	private LogHelper logger;
	private String peerId;

	private boolean connectionEstablished = false;

	private static volatile PeerController instance = null;

	private PeerController(String peerId){
		this.peerId = peerId;
	}

	/**
	 * returnSingletonInstance: returns a singleton peerController instance for the
	 * given peer
	 * 
	 * @param peerID
	 * @return
	 */
	// Required
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
	// Required
	public void beginPeerProcess() {
		// start the current peer server
		new Thread(peerServer).start();

		//Now connect to all previously started peers
		HashMap<String, PeerInfo> peerInfoMap = peerInfoHelperObj.getPeerInfoMap();

		try {
			int currentPeerId = Integer.parseInt(peerId);
			for (Map.Entry<String, PeerInfo> set : peerInfoMap.entrySet()) {
				if (Integer.parseInt(set.getKey()) < currentPeerId) {
					establishConnection(peerInfoMap.get(set.getKey()));
				}
			}

			setAllPeersConnection(true);

		} catch (Exception e) {
			System.out.printf("Exception occured while creating connections with neighbours. Message: %s\n",
					e.getMessage());
		}
	}
	/**
	 * connection to neighbor peer.
	 *
	 * @param peerInfo
	 */
	// Required Change also
	private void establishConnection(PeerInfo peerInfo) throws IOException {
		Socket neighborPeer = new Socket(peerInfo.getAddress(), peerInfo.getPort());
		ConnectionManager getNewPeerHandler = ConnectionManager.createNewInstance(neighborPeer, this);

		getNewPeerHandler.setPeerId(peerInfo.getPeerId());
		connectionManagers.add(getNewPeerHandler);

		new Thread(getNewPeerHandler).start();
	}

	//Required
	private void configPieceManager(boolean isFileExists) {
		this.pieceManager = PieceManager.returnSingletonInstance(isFileExists, peerId);
	}

	//Required
	private boolean configControler() {
		peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance();
		PeerInfo currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
		boolean isFileExists = currPeer != null && currPeer.isFileExist();
		this.connectionManagers = new ArrayList<ConnectionManager>();

		// configure piece manager based on whether the peer has the target file or not
		configPieceManager(isFileExists);
		if (pieceManager == null) {
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
	 * register peerHandler into connectionManagers list
	 * 
	 * @param connectionManager
	 */
	public synchronized void addPeerHandler(ConnectionManager connectionManager) {
		connectionManagers.add(connectionManager);
	}
	/**
	 *
	 * @return
	 */
	// Required Change
	public int getMaxNewConnectionsCount() {
		HashMap<String, PeerInfo> neighborPeerMap = peerInfoHelperObj.getPeerInfoMap();
		Set<String> peerIDList = neighborPeerMap.keySet();

		int count = 0;
		for (Iterator<String> iterator = peerIDList.iterator(); iterator.hasNext();) {
			String peerIdTmp = iterator.next();
			if (Integer.parseInt(peerIdTmp) > Integer.parseInt(peerId)) {
				count++;
			}
		}

		return count;
	}

	// Required
	public boolean isOperationFinish() {
		return false;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setAllPeersConnection(boolean isAllPeersConnection) {
		this.connectionEstablished = isAllPeersConnection;
	}

	// Required
	public synchronized LogHelper getLogger() {
		return logger;
	}

	public boolean isConnection() {
		return connectionEstablished;
	}
}
