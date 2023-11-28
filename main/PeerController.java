package main;

import java.io.*;
import java.net.*;
import java.util.*;

import main.constants.*;
import main.helper.*;
import main.messageTypes.*;
/**
 * The PeerController class manages peer connections and file download operations.
 * It provides methods for checking the status of operations, marking file downloads as complete,
 * and retrieving information about connected peers.
 *
 * 
 * Note: This class ensures thread safety for critical operations through synchronization.
 *
 *
 * @author Adithya KNG, Sharan Sai Reddy Konda, Bhavan Voram
 * @version 1.0
 */
public class PeerController {
	private PeerInfoHelper peerInfoHelperObj;

	private ArrayList<String> chokedPeers = new ArrayList<String>();
	private ArrayList<PeerHandler> listPeerHandlers;

	private OptimisticUnchokePeerHelper optimisticUnchokeHelper;
	private PeerServer peerServer;
	private LogHelper logHelper;
	private String peerId;

	private boolean isConnectionDone = false;
	private ChokeUnchokePeerHelper chokeUnchokeHelper;
	public boolean isDownloadComplete = false;

	private static volatile PeerController instance = null;
	private final HashMap<String, String> allPeerInfoMap = new HashMap<String, String>();
	private PieceHelper pieceHelper;

	/**
	 * Sends a HAVE message to all connected peers except the one with the given peer ID.
	 *
	 * @author Sharan Sai Reddy KOnda
	 * @param index       The index of the piece that the sender now has.
	 * @param givenPeerId The ID of the peer to exclude from receiving the HAVE message.
	 */
	public void sendHaveToAllExcept(int index, String givenPeerId) {
		PeerMessage haveMessage = PeerMessage.create(Constants.TYPE_HAVE_MESSAGE,index);
		for(PeerHandler peerHandler: listPeerHandlers){
			if (!(givenPeerId.equals(peerHandler.getPeerId()))) {
				peerHandler.initiateHaveMessage(haveMessage);
			}
		}
	}

	/**
	 * Retrieves the list of choked peers.
	 *
	 * @author Sharan Sai Reddy Konda
	 * @return An ArrayList containing the names or identifiers of choked peers.
	 */
	public ArrayList<String> returnChokedPeers() {
		return chokedPeers;
	}

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
	 * Retrieves the synchronized instance of the LogHelper class.
	 * 
	 * @author Adithya KNG
	 * @return A LogHelper instance, ensuring thread safety through synchronization.
	 */
	public synchronized LogHelper getLoggerInstance() {
		return logHelper;
	}

	/**
	 * Start the peer process and create a
	 * new thread to connect to neighours
	 * @author Bhavan Voram
	 */
	public void beginPeerProcess() {

		// Start Peerserver
		new Thread(peerServer).start();
		// Start the connection to all previous neighbours
		startPerviousPeersAndComposeManagers();
	}
	/*
	 * Start connection to previous peers and 
	 * confifure the managers
	 * @author Adithya KNG
	 */

	 public void startPerviousPeersAndComposeManagers(){
		try{
			startConnectingToPreviousPeers();
			configureChokeUnchokePeerHelper();
			configureOptimisticUnchokePeerHelper();
		}catch(Exception e){
			System.out.println("Some exception occured "+e.getMessage());
		}
	}

	/*
	 * Configure the chokeUnchoke manager
	 * @author Adithya KNG
	 */
	public void configureChokeUnchokePeerHelper(){
		chokeUnchokeHelper = ChokeUnchokePeerHelper.returnSingletonInstance(this);
		if (!(chokeUnchokeHelper == null)) {
			String unchokeIntervalDelay = CommonConfigHelper.getConfig(Constants.CHOKE_UNCHOKE_INTERVAL_LABEL);
			chokeUnchokeHelper.start(0, Integer.valueOf(unchokeIntervalDelay));
		}
	}

	/*
	 * Confiture the optimistic unchoke manager
	 * @author Sharan Sai Reddy Konda
	 */
	public void configureOptimisticUnchokePeerHelper(){
		optimisticUnchokeHelper = OptimisticUnchokePeerHelper.returnSingletonInstance(this);
		if (!(optimisticUnchokeHelper == null)) {
			String unchokeIntervalDelay = CommonConfigHelper.getConfig(Constants.OPTIMISTIC_UNCHOKE_INTERVAL_LABEL);
			optimisticUnchokeHelper.start(0, Integer.valueOf(unchokeIntervalDelay));
		}
	}
	

	/**
	 * Connect to previous peer neighbors as per the project requirement.
	 */
	private void startConnectingToPreviousPeers() throws IOException {
		HashMap<String, Peer> peerInfoMap = peerInfoHelperObj.getPeerMap();
		int currentPeerId = Integer.valueOf(peerId);
		for (Map.Entry<String, Peer> set : peerInfoMap.entrySet()) {
			int neighborPeerId = Integer.valueOf(set.getKey());
			if ( neighborPeerId < currentPeerId) {
				Peer givenPeer = peerInfoMap.get(set.getKey());
				connectToPeer(givenPeer.getAddress(), givenPeer.getPort(), givenPeer.getPeerId());
			}
		}
		this.isConnectionDone = true;
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
		PeerHandler peerHandlerInstance = PeerHandler.createNewPeerHandler(this,neighborPeer);
		peerHandlerInstance.setPeerId(peerId);
		listPeerHandlers.add(peerHandlerInstance);
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
		peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance(); // get log instance
		Peer currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
		boolean isFileExists = currPeer != null && currPeer.hasFile();
		this.listPeerHandlers = new ArrayList<PeerHandler>();

		// configure piece manager based on whether the peer has the target file or not
		configPieceHelper(isFileExists);
		if (pieceHelper == null) {
			return false;
		}

		// configure logger instance
		logHelper = new LogHelper(peerId);
		if (!logHelper.isLoggerInitialized()) {
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
		chokeUnchokeHelper.destroy();
	}

	/**
	 * Checks whether the connection with all peers is complete.
	 *
	 * @author Bhavan Voram
	 * @return True if the connection with all peers is complete, false otherwise.
	 */
	public boolean isConnectionDone() {
		return isConnectionDone;
	}

	/**
	 * Checks whether the download of the file is complete.
	 *
	 * @author Sharan Sai Reddy Konda
	 * @return True if the file download is complete, false otherwise.
	 */
	public boolean isFileDownloadComplete() {
		return pieceHelper.hasDownloadFileComplete();
	}
	
	/*
	 * Termiante Optimistic unchoke Peer Helper
	 * @author Adithya KNG
	 */
	private void terminateOptimisticUnchokePeerHelper() {
		optimisticUnchokeHelper.destroy();
	}
	
	/*
	 * Terminate the logger
	 * @author Bhavan Voram
	 */
	private void terminateLogger() {
		logHelper.destroy();
	}

	/*
	 * Create and return a peer message in sychronized fashion
	 * @return PeerMessage
	 * @author Adithya KNG
	 */
	public PeerMessage getPeerMessage() {
		PeerMessage message;
		synchronized (this) {
			message = PeerMessage.create(Constants.TYPE_BITFIELD_MESSAGE);
			message.setBitFieldHandler(pieceHelper.getBitFieldHelper());
		}
		return message;
	}

	/**
	 * Get the Download Rates of all the peers
	 * @return map of peer ids and download rates
	 * @author Bhavan Voram
	 */
	public HashMap<String, Double> getDownloadRates() {
		HashMap<String, Double> downloadRates = new HashMap<String, Double>();
		if(listPeerHandlers.size() == 0){
			return downloadRates;
		}
		for(PeerHandler peerHandler: listPeerHandlers){
			downloadRates.put(peerHandler.getPeerId(), peerHandler.getDownloadSpeed());
		}
		return downloadRates;
	}

	/**
	 * Initiates optimistic unchoking for a specified contender peer.
	 *
	 * @author Sharan Sai Reddy Konda
	 * @param contenderID The ID of the contender peer to be optimistically unchoked.
	 */
	
	public void optimisticallyUnChokePeersWithContender(String contenderID) {
		// Log the change of optimistically unchoked neighbors
		logHelper.logMessage(
				String.format(Constants.CHANGE_OF_OPTIMISTICALLY_UNCHOKED_NEIGHBORS_LOG_MESSAGE, peerId, contenderID));
		
		// Find the specified contender in the list of peer handlers and send an unchoke message
		for (PeerHandler peerHandler : listPeerHandlers) {
			if (peerHandler.getPeerId().equals(contenderID)) {
				if (peerHandler.hasReceivedHandshake()) {
					peerHandler.initiateUnchokeMessage(createMessage(Constants.TYPE_UNCHOKE_MESSAGE));
				}
				break;
			}
		}
	}

	/**
	 * Checks whether the operation has completed.
	 * 
	 * @author Sharan Sai Reddy Konda
	 * @return True if the operation is considered complete, false otherwise.
	 */
	public boolean checkIfOperationComplete() {
		return false;
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
	 * Handles the choke or unchoke operation for a list of peer IDs based on the specified type.
	 *
	 * @param listPeerIds A list of peer IDs to be processed.
	 * @param type        The type of message to be sent (Constants.TYPE_CHOKE_MESSAGE or Constants.TYPE_UNCHOKE_MESSAGE).
	 * @author Adithya KNG
	 */
	private void handleChokeUnchokePeers(ArrayList<String> listPeerIds, byte type){
		for(String id: listPeerIds){
			int index = 0;
			while(index < listPeerHandlers.size()){
				if (listPeerHandlers.get(index).getPeerId().equals(id)) {
					if (listPeerHandlers.get(index).hasReceivedHandshake()) {
						if(type == Constants.TYPE_CHOKE_MESSAGE){
							listPeerHandlers.get(index).initiateChokeMessage(createMessage(type));
							break;
						}
						if(type == Constants.TYPE_UNCHOKE_MESSAGE){
							listPeerHandlers.get(index).initiateUnchokeMessage(createMessage(type));
							break;
						}
					} else {
						break;
					}
				}
				index++;
			}
		}
	}

	/**
	 * Updates the list of choked peers and triggers the choke operation for the specified peer IDs.
	 *
	 * @param listPeerIds A list of peer IDs to be set as choked peers.
	 * @author Sharan Sai Reddy Konda
	 */
	public void getAndSetChokePeers(ArrayList<String> listPeerIds) {
		chokedPeers = listPeerIds;
		handleChokeUnchokePeers(listPeerIds, Constants.TYPE_CHOKE_MESSAGE);
	}

	/**
	 * Initiates the unchoking operation for the specified preferred peer IDs.
	 *
	 * @param listPeerIds A list of preferred peer IDs to be unchoked.
	 * @author Bhavan Voram
	 */
	public void unchokePreferredPeers(ArrayList<String> listPeerIds) {
		handleChokeUnchokePeers(listPeerIds, Constants.TYPE_UNCHOKE_MESSAGE);
	}

	/**
	 * Inserts a piece into the piece manager, updating the bit field and logging the progress.
	 * 
	 * @author Adithya KNG
	 * @param pieceMessage  The PeerMessage containing the piece to be inserted.
	 * @param sourcePeerID  The ID of the peer that sent the piece.
	 */
	public void receiveAndStorePiece(PeerMessage pieceMessage, String sourcePeerID) {
		synchronized(this){
			try {
				pieceHelper.insertNthPiece(pieceMessage.getIndex(), pieceMessage.getData());
				logHelper.logMessage(String.format(Constants.FILE_PARTIAL_DOWNLOADE_LOG_MESSAGE, instance.getPeerId(),
						pieceMessage.getIndex(), sourcePeerID,
						pieceHelper.getBitFieldHelper().getCountOfDownloadedSegments()));
			} catch (IOException e) {
				System.out
						.println("Exception occured while inserting the piece at nth position for the given peer. Message: "
								+ e.getMessage());
			}
		}
	}

	/**
     * Generates a PeerMessage containing a data segment (piece) from the specified index.
     *
     * @author Adithya KNG
     * @param index The index of the data segment (piece) to be included in the PeerMessage.
     * @return A PeerMessage containing the data segment, or null if an exception occurs.
     */
	public PeerMessage constructPieceMessage(int index) {
		try {
			Piece dataSegment = pieceHelper.getNthPieceFromFile(index);
			if(dataSegment == null){
				return null;
			}
			return PeerMessage.create(Constants.TYPE_PIECE_MESSAGE,index, dataSegment);
		} catch (IOException e) {
			System.out.println(
					"Exception occured while extracting or generating piece message. Message: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Informs all other connected peers to terminate the connection as the current peer
	 * will be shutting down due to task completion.
	 * Marks the current peer as having successfully downloaded the file.
	 * Sends shutdown messages to all other peers, allowing them to request missing pieces
	 * from alternative sources.
	 * 
	 * @author Adithya KNG
	 */
	public void notifyPeersAboutShutdown() {
		if(isConnectionDone() && peerServer.getServerStatus()){
			// Mark that the current peer has successfully downloaded the file.
			confirmFileDownload(peerId);
			// Send shutdown messages to all other peers so that they can request the
			// missing pieces from other peers.
			for (PeerHandler peerHandler : listPeerHandlers) {
				peerHandler.initiateShutdownMessage(createMessage(Constants.TYPE_SHUTDOWN_MESSAGE));
			}
		}
	}

	/**
	 * Calculates and returns the count of potential new connections based on the peers with
	 * IDs greater than the current peer's ID.
	 *
	 * @author Adithya KNG
	 * @return The count of potential new connections.
	 */
	public int calculatePossibleNewConnections() {
		int number = 0;
		int intPeerId = Integer.valueOf(peerId);
		for(String peerMapId :  peerInfoHelperObj.getPeerMap().keySet()){
			if(Integer.valueOf(peerMapId) > intPeerId){
				number += 1;
			}
		}
		return number;
	}

	/**
	 * Marks the file download as completed for a specific peer.
	 * 
	 * @author Adithya KNG
	 * @param id The identifier or id of the peer for which the file download is marked as complete.
	 */
	public synchronized void confirmFileDownload(String id) {
		allPeerInfoMap.put(id, " ");
	}

	/**
	 * Retrieves the unique identifier of the current peer.
	 * 
	 * @author Bhavan Voram
	 * @return A string representing the peer identifier.
	 */
	public String getPeerId() {
		return peerId;
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
	public synchronized void setPeerToHandler(PeerHandler handler) {
		listPeerHandlers.add(handler);
	}

}
