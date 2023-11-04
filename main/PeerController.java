package main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import main.constants.Constants;
import main.handlers.PeerHandler;
import main.helper.ChokeUnchokePeerHelper;
import main.helper.CommonConfigHelper;
import main.helper.LogHelper;
import main.helper.OptimisticUnchokePeerHelper;
import main.helper.PeerInfoHelper;
import main.helper.PieceHelper;
import main.messageTypes.PeerMessage;
import main.messageTypes.Peer;
import main.messageTypes.Piece;

/**
 * Controller
 */
public class PeerController {

	private ArrayList<PeerHandler> peerHandlers;
	private PieceHelper pieceManager;
	private PeerInfoHelper peerInfoHelperObj;

	private final HashMap<String, String> peerCompleteMap = new HashMap<String, String>();
	private ArrayList<String> chokedPeers = new ArrayList<String>();

	private ChokeUnchokePeerHelper chokeUnchokeManager;
	private OptimisticUnchokePeerHelper optimisticUnchokeManager;
	private PeerServer peerServer;
	private LogHelper logger;
	private String peerId;

	private boolean connectionEstablished = false;

	private static volatile PeerController instance = null;

	/**
	 * returnSingletonInstance: returns a singleton peerController instance for the
	 * given peer
	 * 
	 * @param peerID
	 * @return
	 */
	public static synchronized PeerController returnSingletonInstance(String peerID) {
		if (instance == null) {
			instance = new PeerController();
			instance.peerId = peerID;
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

		connectToPreviousPeer(); // connect to peer neighbors

		chokeUnchokeManager = ChokeUnchokePeerHelper.returnSingletonInstance(this);
		if (chokeUnchokeManager != null) {
			int chokeUnchokeInterval = Integer
					.parseInt(CommonConfigHelper.getConfig(Constants.CHOKE_UNCHOKE_INTERVAL_LABEL));
			chokeUnchokeManager.start(0, chokeUnchokeInterval);

		}

		optimisticUnchokeManager = OptimisticUnchokePeerHelper.returnSingletonInstance(this);
		if (optimisticUnchokeManager != null) {
			int optimisticUnchokeInterval = Integer
					.parseInt(CommonConfigHelper.getConfig(Constants.OPTIMISTIC_UNCHOKE_INTERVAL_LABEL));
			optimisticUnchokeManager.start(0, optimisticUnchokeInterval);
		}
	}

	/**
	 * Connect to previous peer neighbors as per the project requirement.
	 */
	private void connectToPreviousPeer() {
		HashMap<String, Peer> peerInfoMap = peerInfoHelperObj.getPeerMap();

		try {
			for (Map.Entry<String, Peer> set : peerInfoMap.entrySet()) {
				if (Integer.parseInt(set.getKey()) < Integer.parseInt(peerId)) {
					makeConnection(peerInfoMap.get(set.getKey()));
				}
			}

			setAllPeersConnection(true);

		} catch (IOException e) {
			System.out.printf("Exception occured while creating connections with neighbours. Message: %s\n",
					e.getMessage());
		}
	}

	/**
	 * connection to neighbor peer.
	 *
	 * @param peerInfo
	 */
	private void makeConnection(Peer peerInfo) throws IOException {
		String address = peerInfo.getAddress();
		int port = peerInfo.getPort();

		Socket neighborPeer = new Socket(address, port);
		PeerHandler peerHandlerTmp = PeerHandler.getNewInstance(neighborPeer, this);

		peerHandlerTmp.setPeerId(peerInfo.getPeerId());
		peerHandlers.add(peerHandlerTmp);

		new Thread(peerHandlerTmp).start();
	}

	private void configPieceManager(boolean isFileExists) {
		this.pieceManager = PieceHelper.returnSingletonInstance(isFileExists, peerId);
	}

	private boolean configControler() {
		peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance(); // get log instance
		Peer currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
		boolean isFileExists = currPeer != null && currPeer.hasFile();
		this.peerHandlers = new ArrayList<PeerHandler>();

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
	 * This function checks if the file is downloaded by the peers or not and
	 * updates accordingly.
	 * 
	 */
	public void updateFileDownloadStatus() {
		if (isConnection() == false || peerServer.getServerStatus() == false) {
			return;
		}

		if (peerInfoHelperObj.getPeerMap().size() == peerCompleteMap.size()) {
			this.terminateObjects();
		}
	}

	/**
	 * This function terminates all the necessary objects by closing and freeing
	 * them out from memory and exits the process safely.
	 */
	public void terminateObjects() {
		chokeUnchokeManager.destroy();
		optimisticUnchokeManager.destroy();
		logger.destroy();
		pieceManager.close();
		System.exit(0);
	}

	/**
	 * register peerHandler into peerHandlers list
	 * 
	 * @param peerHandler
	 */
	public synchronized void addPeerHandler(PeerHandler peerHandler) {
		peerHandlers.add(peerHandler);
	}

	/**
	 * generate BitFieldMessage
	 * 
	 * @return
	 */
	public synchronized PeerMessage getBitFieldMessage() {
		PeerMessage message = PeerMessage.create();

		message.setMessageType(Constants.TYPE_BITFIELD_MESSAGE);
		message.setBitFieldHandler(pieceManager.getBitFieldHelper());

		return message;
	}

	/**
	 * gen download speed map
	 * 
	 * @return
	 */
	public HashMap<String, Double> getDownloadRates() {
		HashMap<String, Double> speedList = new HashMap<>();
		for (int i = 0; i < peerHandlers.size(); i++) {
			PeerHandler peerHandler = peerHandlers.get(i);
			speedList.put(peerHandler.getPeerId(), peerHandler.downloadSpeed());
		}
		return speedList;
	}

	/**
	 * setChokePeers
	 * 
	 * @param peerList
	 */
	public void setChokePeers(ArrayList<String> peerList) {
		chokedPeers = peerList;

		PeerMessage chokeMessage = PeerMessage.create();
		chokeMessage.setMessageType(Constants.TYPE_CHOKE_MESSAGE);

		for (int i = 0; i < peerList.size(); i++) {
			String peerIdTmp = peerList.get(i);
			for (int j = 0, peerHandlersSize = peerHandlers.size(); j < peerHandlersSize; j++) {
				PeerHandler peerHandler = peerHandlers.get(j);
				if (peerHandler.getPeerId().equals(peerIdTmp)) {
					if (peerHandler.isHandshakeReceived()) {
						// System.out.println(LOGGER_PREFIX+" : Sending CHOKE message to peers :
						// "+peerToBeChoked);
						peerHandler.sendChokeMessage(chokeMessage);
						break;
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * unChokePeers
	 * 
	 * @param peerList
	 */
	public void unChokePeers(ArrayList<String> peerList) {
		PeerMessage unChokeMessage = PeerMessage.create();
		unChokeMessage.setMessageType(Constants.TYPE_UNCHOKE_MESSAGE);
		// System.out.println(LOGGER_PREFIX+" : Sending UNCHOKE message to peers...");
		for (int i = 0; i < peerList.size(); i++) {
			String peerToBeUnChoked = peerList.get(i);
			for (int j = 0; j < peerHandlers.size(); j++) {
				PeerHandler peerHandler = peerHandlers.get(j);
				if (peerHandler.getPeerId().equals(peerToBeUnChoked)) {
					if (peerHandler.isHandshakeReceived()) {
						// System.out.println(LOGGER_PREFIX+" : Sending UNCHOKE message to
						// peers..."+peerToBeUnChoked);
						peerHandler.sendUnchokeMessage(unChokeMessage);
						break;
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * this function tries to unchoke the peer chosen optimistically and connects
	 * with it if handshake is successful
	 * 
	 * @param contenderID - the peer ID of the optimistically chosen unchoke peer.
	 * @return null
	 */
	public void optimisticallyUnChokePeers(String contenderID) {
		PeerMessage unChokeMessage = PeerMessage.create();
		unChokeMessage.setMessageType(Constants.TYPE_UNCHOKE_MESSAGE);

		logger.logMessage(String.format(Constants.OPTIMISTICALLY_UNCHOKE_LOG_MESSAGE, peerId, contenderID));
		for (PeerHandler peerHandler : this.peerHandlers) {
			if (peerHandler.getPeerId().equals(contenderID)) {
				if (peerHandler.isHandshakeReceived()) {
					peerHandler.sendUnchokeMessage(unChokeMessage);
				}
				break;
			}
		}
	}

	/**
	 * insert piece to piece manager
	 * 
	 * @param pieceMessage
	 * @param sourcePeerID
	 */
	public synchronized void insertPiece(PeerMessage pieceMessage, String sourcePeerID) {
		try {
			pieceManager.insertNthPiece(pieceMessage.getIndex(), pieceMessage.getData());
			logger.logMessage("Peer [" + instance.getPeerId() + "] has downloaded the piece [" + pieceMessage.getIndex()
					+ "] from [" + sourcePeerID + "]. Now the number of pieces it has is "
					+ (pieceManager.getBitFieldHelper().getCountOfDownloadedSegments()));
		} catch (IOException e) {
			System.out
					.println("Exception occured while inserting the piece at nth position for the given peer. Message: "
							+ e.getMessage());
		}
	}

	/**
	 * generate PieceMessage
	 * 
	 * @param index
	 * @return
	 */
	public PeerMessage genPieceMessage(int index) {
		try {
			Piece dataSegment = pieceManager.getNthPieceFromFile(index);
			if (dataSegment != null) {
				PeerMessage message = PeerMessage.create();
				message.setData(dataSegment);
				message.setIndex(index);
				message.setMessageType(Constants.TYPE_PIECE_MESSAGE);
				return message;
			}

			return null;
		} catch (IOException e) {
			System.out.println(
					"Exception occured while extracting or generating piece message. Message: " + e.getMessage());
			return null;
		}
	}

	/**
	 * send HaveMessage
	 * 
	 * @param pieceIndex
	 * @param fromPeerID
	 */
	public void sendHaveMessage(int pieceIndex, String fromPeerID) {
		PeerMessage haveMessage = PeerMessage.create();
		haveMessage.setIndex(pieceIndex);
		haveMessage.setMessageType(Constants.TYPE_HAVE_MESSAGE);

		for (int i = 0, peerHandlersSize = peerHandlers.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = peerHandlers.get(i);
			// System.out.println(LOGGER_PREFIX+": Sending have message from "+peerID+" to :
			// "+peerHandler.getPeerId());
			if (fromPeerID.equals(peerHandler.getPeerId())) {
				continue;
			}
			peerHandler.sendHaveMessage(haveMessage);
		}
	}

	/**
	 * This function informs all other peers to terminate the connection as the
	 * current peer will be shutting down due to completion of task.
	 */
	public void broadcastShutdown() {
		if (isConnection() == false || peerServer.getServerStatus() == false) {
			return;
		}

		// Create a shutdown mesage
		PeerMessage shutdownMessage = PeerMessage.create();
		shutdownMessage.setMessageType(Constants.SHUTDOWN_MESSAGE);

		// Mark that the current peer has successfully downloaded the file.
		markFileDownloadComplete(peerId);

		// Send shutdown messages to all other peers so that they can request the
		// missing pieces from other peers.
		for (PeerHandler peerHandler : peerHandlers) {
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getMaxNewConnectionsCount() {
		HashMap<String, Peer> neighborPeerMap = peerInfoHelperObj.getPeerMap();
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

	public boolean isOperationFinish() {
		return false;
	}

	public synchronized void markFileDownloadComplete(String peer) {
		// System.out.println("before, peerCompleteMap.size()="+peerCompleteMap.size());
		peerCompleteMap.put(peer, " ");
		// System.out.println("after, peerCompleteMap.size()="+peerCompleteMap.size());
		// for(HashMap.Entry<String,String> entry : peerCompleteMap.entrySet())
		// System.out.println("Key = " + entry.getKey() + ", Value = " +
		// entry.getValue());
	}

	public String getPeerId() {
		return peerId;
	}

	public void setAllPeersConnection(boolean isAllPeersConnection) {
		this.connectionEstablished = isAllPeersConnection;
	}

	public ArrayList<String> getChokedPeers() {
		return chokedPeers;
	}

	public synchronized LogHelper getLogger() {
		return logger;
	}

	public boolean isConnection() {
		return connectionEstablished;
	}

	public boolean isFileDownloadComplete() {
		return pieceManager.hasDownloadFileComplete();
	}

}
