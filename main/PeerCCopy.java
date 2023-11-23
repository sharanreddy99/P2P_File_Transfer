package main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import main.constants.Constants;
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
public class PeerCCopy {

	private ArrayList<PeerHandler> listPeerHandler;
	private PieceHelper pieceHelper;
	private PeerInfoHelper peerInfoHelperObj;

	private final HashMap<String, String> allPeerInfoMap = new HashMap<String, String>();
	private ArrayList<String> allChokedPeerIds = new ArrayList<String>();

	private ChokeUnchokePeerHelper chokeUnchokePeerHelper;
	private OptimisticUnchokePeerHelper optimisticUnchokePeerHelper;
	private PeerServer peerServer;
	private LogHelper logger;
	private String peerId;

	private boolean isConnectionDone = false;
	public boolean isDownloadComplete = false;

	private static volatile PeerController instance = null;

	/**
	 * Set PeerID to this instance
	 * Compose the controller
	 * @author Adithya KNG
	 * @param id
	 */
	private void attachPeerIdAndComposeController(String id){
		this.peerId = id;
		boolean controllerConfigured = instance.composeController();
		if (!controllerConfigured) {
			instance = null;
		}
	}

	/**
	 * returnSingletonInstance: returns a singleton peerController instance for the
	 * given peer
	 * 
	 * @param peerID
	 * @return
	 * @author Adithya KNG
	 */
	public static synchronized PeerController returnSingletonInstance(String peerID) {
		if(instance != null){
			return null;
		}
		instance = new PeerController();
		instance.attachPeerIdAndComposeController(peerID);
		return instance;
	}

	/**
	 * Start the peer process and create a
	 * new thread to connect to neighours
	 * @author Bhavan Voram
	 */
	public void beginPeerProcess() {
		// start the current peer server
		new Thread(peerServer).start();
		// Start the connection to all previous neighbours
		startPerviousPeersAndComposeManagers();
	}

	/*
	 * Configure the chokeUnchoke manager
	 * @author Adithya KNG
	 */
	public void configureChokeUnchokePeerHelper(){
		chokeUnchokePeerHelper = ChokeUnchokePeerHelper.returnSingletonInstance(this);
		if (!(chokeUnchokePeerHelper == null)) {
			String unchokeIntervalDelay = CommonConfigHelper.getConfig(Constants.CHOKE_UNCHOKE_INTERVAL_LABEL);
			chokeUnchokePeerHelper.start(0, Integer.valueOf(unchokeIntervalDelay));
		}
	}
	
	/*
	 * Confiture the optimistic unchoke manager
	 * @author Sharan Sai Reddy Konda
	 */
	public void configureOptimisticUnchokePeerHelper(){
		optimisticUnchokePeerHelper = OptimisticUnchokePeerHelper.returnSingletonInstance(this);
		if (!(optimisticUnchokePeerHelper == null)) {
			String unchokeIntervalDelay = CommonConfigHelper.getConfig(Constants.OPTIMISTIC_UNCHOKE_INTERVAL_LABEL);
			optimisticUnchokePeerHelper.start(0, Integer.valueOf(unchokeIntervalDelay));
		}
	}

	/*
	 * Start connection to previous peers and 
	 * confifure the managers
	 * @author Adithya KNG
	 */

	public void startPerviousPeersAndComposeManagers(){
		startConnectingToPreviousPeers();
		configureChokeUnchokePeerHelper();
		configureOptimisticUnchokePeerHelper();
	}

	/**
	 * Start connection to Previous Peers
	 * @author Adithya KNG
	 */
	private void startConnectingToPreviousPeers() {
		TreeMap<String, Peer> peerInfoMap = peerInfoHelperObj.getPeerMap();
		int currentPeerId = Integer.valueOf(peerId);
		try {
			for (Map.Entry<String, Peer> set : peerInfoMap.entrySet()) {
				int neighborPeerId = Integer.valueOf(set.getKey());
				if ( neighborPeerId > currentPeerId) {
					// do nothing
				}
				else{
					// Connect to the neighbour with the given peer information
					Peer givenPeer = peerInfoMap.get(set.getKey());
					connectToPeer(givenPeer.getAddress(), givenPeer.getPort(), givenPeer.getPeerId());
				}
			}

			// Once done set connection done to true
			this.isConnectionDone = true;

		} catch (IOException e) {
			System.out.printf("Exception occured while creating connections with neighbours. Message: %s\n",
					e.getMessage());
		}
	}

	/**
	 * Function to connect a peer to it's neighbours
	 * @author AdithyaKNG
	 * @param address
	 * @param port
	 * @param peerId
	 * @throws IOException
	 */
	private void connectToPeer(String address, int port, String peerId) throws IOException {
		Socket neighborPeer = new Socket(address, port);
		PeerHandler peerHandlerInstance = getPeerHandler(peerId, neighborPeer);
		Thread peerHandlerThread = new Thread(peerHandlerInstance);
		peerHandlerThread.start();
	}

	/**
	 * Function to get the peer handler
	 * @author Bhavan Voram
	 * @param peerId
	 * @param neighborPeer
	 * @return
	 */
	private PeerHandler getPeerHandler(String peerId, Socket neighborPeer){
		PeerHandler peerHandlerInstance = PeerHandler.getNewInstance(neighborPeer, this);
		peerHandlerInstance.setPeerId(peerId);
		listPeerHandler.add(peerHandlerInstance);
		return peerHandlerInstance;
	}


	/**
	 * Function to initilize the piece manager
	 * @author Sharan Sai Reddy Konda
	 * @param isFileExists
	 */
	private void configPieceHelper(boolean isFileExists) {
		this.pieceHelper = PieceHelper.returnSingletonInstance(isFileExists, peerId);
	}

	/**
	 * Function to start the Peer controller.
	 * @author Sharan Sai Reddy Konda
	 * @return boolean
	 */
	private boolean composeController() {
		peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance();
		Peer currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
		boolean isFileExists = currPeer != null && currPeer.hasFile();
		this.listPeerHandler = new ArrayList<PeerHandler>();

		// configure piece manager based on whether the peer has the target file or not
		configPieceHelper(isFileExists);
		if (pieceHelper == null) {
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

	/*
	 * This function checks if the file is downloaded by the peers or not and
	 * updates accordingly.
	 * @return boolean
	 * @author Adithya KNG
	 */
	public boolean updateFileDownloadStatus() {
		boolean connectionStatus = isConnectionDone();
		boolean serverStatus = peerServer.getServerStatus();
		
		if(connectionStatus == false || serverStatus == false){
			// do nothing and return
			return false;
		}
		else{
			int peerMapSize = peerInfoHelperObj.getPeerMap().size();
			int allPeerInfoMapSize = allPeerInfoMap.size();
			return (peerMapSize == allPeerInfoMapSize) ? this.terminateObjects() : false;
		}
	}

	/**
	 * This function terminates all the necessary objects by closing and freeing
	 * them out from memory and exits the process safely.
	 */
	public boolean terminateObjects() {
		try {
			// Perform cleanup for each object
			terminateChokeUnchokePeerHelper();
			terminateOptimisticUnchokePeerHelper();
			terminateLogger();
			terminatePieceHelper();
	
			System.exit(0);
			// Optionally, perform any additional cleanup or resource release
			return true; // Indicate successful termination
		} catch (Exception e) {
			// Handle exceptions or log errors
			return false; // Indicate unsuccessful termination
		}
	}
	
	/*
	 * Terminate Choke and Unchoke Peerhelper
	 * @author Adithya KNG
	 */
	private void terminateChokeUnchokePeerHelper() {
		chokeUnchokePeerHelper.destroy();
	}
	
	/*
	 * Termiante Optimistic unchoke Peer Helper
	 * @author Adithya KNG
	 */
	private void terminateOptimisticUnchokePeerHelper() {
		optimisticUnchokePeerHelper.destroy();
	}
	
	/*
	 * Terminate the logger
	 * @author Bhavan Voram
	 */
	private void terminateLogger() {
		logger.destroy();
	}
	
	/*
	 * Terminate the piece helper
	 * @author Sharan Sai Reddy Konda
	 */
	private void terminatePieceHelper() {
		pieceHelper.close();
	}
	

	/**
	 * Add the peerhandler into the list of peer handlers
	 * @author Sharan Sai Reddy Konda
	 * @param peerHandler
	 */
	public synchronized void addPeerHandler(PeerHandler peerHandler) {
		listPeerHandler.add(peerHandler);
	}

	/*
	 * Create and return a peer message in sychronized fashion
	 * @return PeerMessage
	 * @author Adithya KNG
	 */
	public synchronized PeerMessage getPeerMessage() {
		PeerMessage message = PeerMessage.create();

		message.setMessageType(Constants.TYPE_BITFIELD_MESSAGE);
		message.setBitFieldHandler(pieceHelper.getBitFieldHelper());

		return message;
	}

	/**
	 * Get the Download Rates of all the peers
	 * @return map of peer ids and download rates
	 * @author Bhavan Voram
	 */
	public HashMap<String, Double> getDownloadRates() {
		HashMap<String, Double> downloadRates = new HashMap<String, Double>();
		if(listPeerHandler.size() == 0){
			return downloadRates;
		}
		for(PeerHandler peerHandler: listPeerHandler){
			downloadRates.put(peerHandler.getPeerId(), peerHandler.downloadSpeed());
		}
		return downloadRates;
	}

	/*
	 * Function to create message with given constant
	 * 
	 * @param byte constant
	 * @author Sharan Sai Reddy Konda
	 * @return PeerMessage
	 */
	private PeerMessage createMessage(byte constant) {
		PeerMessage message = PeerMessage.create();
		message.setMessageType(constant);
		return message;
	}

	/**
	 * setChokePeers
	 * 
	 * @param peerList
	 */
	public void setChokePeers(ArrayList<String> listPeerIds) {
		allChokedPeerIds = listPeerIds;
		for(String id: listPeerIds){
			for(int i=0; i<listPeerHandler.size(); i++){
				PeerHandler peerHandler = listPeerHandler.get(i);
				if (listPeerHandler.get(i).getPeerId().equals(id)) {
					if (listPeerHandler.get(i).isHandshakeReceived()) {
						peerHandler.sendChokeMessage(createMessage(Constants.TYPE_CHOKE_MESSAGE));
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
			for (int j = 0; j < listPeerHandler.size(); j++) {
				PeerHandler peerHandler = listPeerHandler.get(j);
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

		logger.logMessage(
				String.format(Constants.CHANGE_OF_OPTIMISTICALLY_UNCHOKED_NEIGHBORS_LOG_MESSAGE, peerId, contenderID));
		for (PeerHandler peerHandler : this.listPeerHandler) {
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
			pieceHelper.insertNthPiece(pieceMessage.getIndex(), pieceMessage.getData());
			logger.logMessage(String.format(Constants.FILE_PARTIAL_DOWNLOADE_LOG_MESSAGE, instance.getPeerId(),
					pieceMessage.getIndex(), sourcePeerID,
					pieceHelper.getBitFieldHelper().getCountOfDownloadedSegments()));
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
			Piece dataSegment = pieceHelper.getNthPieceFromFile(index);
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

		for (int i = 0, peerHandlersSize = listPeerHandler.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = listPeerHandler.get(i);
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
		if (isConnectionDone() == false || peerServer.getServerStatus() == false) {
			return;
		}

		// Create a shutdown mesage
		PeerMessage shutdownMessage = PeerMessage.create();
		shutdownMessage.setMessageType(Constants.TYPE_SHUTDOWN_MESSAGE);

		// Mark that the current peer has successfully downloaded the file.
		markFileDownloadComplete(peerId);

		// Send shutdown messages to all other peers so that they can request the
		// missing pieces from other peers.
		for (PeerHandler peerHandler : listPeerHandler) {
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getMaxNewConnectionsCount() {
		TreeMap<String, Peer> neighborPeerMap = peerInfoHelperObj.getPeerMap();
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
		allPeerInfoMap.put(peer, " ");
		// System.out.println("after, peerCompleteMap.size()="+peerCompleteMap.size());
		// for(HashMap.Entry<String,String> entry : peerCompleteMap.entrySet())
		// System.out.println("Key = " + entry.getKey() + ", Value = " +
		// entry.getValue());
	}

	public String getPeerId() {
		return peerId;
	}

	public ArrayList<String> getAllChokedPeerIds() {
		return allChokedPeerIds;
	}

	public synchronized LogHelper getLogger() {
		return logger;
	}

	public boolean isConnectionDone() {
		return isConnectionDone;
	}

	public boolean isFileDownloadComplete() {
		return pieceHelper.hasDownloadFileComplete();
	}

}
