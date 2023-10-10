package main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import main.constants.Constants;
import main.helper.CommonPropertyUtil;
import main.helper.MessageLoggerUtil;
import main.helper.PeerInfoHelper;
import main.manager.ChokeUnchokeManager;
import main.manager.OptimisticUnchokeManager;
import main.manager.filehandler.PieceManager;
import main.manager.peerhandler.PeerHandler;
import main.messageTypes.Peer2PeerMessage;
import main.messageTypes.PeerInfo;
import main.messageTypes.Piece;

/**
 * Controller
 */
public class Controller {

	private ArrayList<PeerHandler> peerHandlers;
	private PieceManager pieceManager;
	private PeerInfoHelper peerInfoPropertyUtil;

	private final HashMap<String, String> peerCompleteMap = new HashMap<String, String>();
	private ArrayList<String> chokedPeers = new ArrayList<String>();

	private ChokeUnchokeManager chokeUnchokeManager;
	private OptimisticUnchokeManager optimisticUnchokeManager;
	private PeerServer peerServer;
	private MessageLoggerUtil logger;
	private String peerId;

	private boolean connectionEstablished = false;

	private static volatile Controller instance = null;

	/**
	 * get instance
	 * 
	 * @param peerID
	 * @return
	 */
	public static synchronized Controller getInstance(String peerID) {
		if (instance == null) {
			instance = new Controller();
			instance.peerId = peerID;
			if (!instance.init()) {
				instance = null;
			}
		}
		return instance;
	}

	/**
	 * start server
	 */
	public void startController() {

		startServer(peerId); // start peer server

		connectToPreviousPeer(); // connect to peer neighbors

		chokeUnchokeManager = ChokeUnchokeManager.getInstance(this);
		assert chokeUnchokeManager != null;
		chokeUnchokeManager.start(0,
				Integer.parseInt(CommonPropertyUtil.getProperty(Constants.CHOKE_UNCHOKE_INTERVAL)));

		optimisticUnchokeManager = OptimisticUnchokeManager.getInstance(this);
		assert optimisticUnchokeManager != null;
		optimisticUnchokeManager.start(0,
				Integer.parseInt(CommonPropertyUtil.getProperty(Constants.OPTIMISTIC_UNCHOKE_INTERVAL)));
	}

	/**
	 * start peer server
	 * 
	 * @param peerId
	 */
	private void startServer(String peerId) {
		new Thread(peerServer).start();
	}

	/**
	 * Connect to previous peer neighbors as per the project requirement.
	 */
	private void connectToPreviousPeer() {
		HashMap<String, PeerInfo> peerMap = peerInfoPropertyUtil.getPeerInfoMap();
		Set<String> peerIdList = peerMap.keySet();

		for (Iterator<String> iterator = peerIdList.iterator(); iterator.hasNext();) {
			String peerIdTmp = iterator.next();
			if (Integer.parseInt(peerIdTmp) < Integer.parseInt(peerId)) {
				makeConnection(peerMap.get(peerIdTmp)); // connect to neighbor
			}
		}
		setAllPeersConnection(true);
	}

	/**
	 * connection to neighbor peer.
	 *
	 * @param peerInfo
	 */
	private void makeConnection(PeerInfo peerInfo) {
		String address = peerInfo.getAddress();
		int port = peerInfo.getPort();

		try {
			// System.out.println(LOGGER_PREFIX + " Connection peer " + peerInfo.getPeerID()
			// + " on " + neighborPeerHost + " port: " + neighborPortNumber);
			Socket socketTmp = new Socket(address, port);
			// System.out.println(LOGGER_PREFIX + " Connected to peer " +
			// peerInfo.getPeerID() + " on " + neighborPeerHost + " port: " +
			// neighborPortNumber);

			PeerHandler peerHandlerTmp = PeerHandler.getNewInstance(socketTmp, this);
			peerHandlerTmp.setPeerId(peerInfo.getPeerId());
			peerHandlers.add(peerHandlerTmp);

			new Thread(peerHandlerTmp).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * init server data
	 * 
	 * @return
	 */
	private boolean init() {
		peerInfoPropertyUtil = PeerInfoHelper.getInstance(); // get log instance
		// init pieceManager
		if (!PeerInfoHelper.getInstance().getPeerInfoMap().get(peerId).isFileExist()) {
			pieceManager = PieceManager.getInstance(false, peerId);
		} else {
			pieceManager = PieceManager.getInstance(true, peerId);
		}

		if (pieceManager == null) {
			return false;
		}

		if ((logger = MessageLoggerUtil.init(peerId)) == null) {
			System.out.println("Unable to Initialize logger object");
			return false;
		}

		peerHandlers = new ArrayList<>();

		// init peerServer
		peerServer = PeerServer.getInstance(peerId, this);

		return true;
	}

	/**
	 * file download completed and shutdown
	 */
	public void fileDownloadComplete() {
		if (!isConnection() || !peerServer.isServerCompleted()) {
			return;
		}
		// System.out.println("peerInfoPropertyUtil.getPeerInfoMap().size()="+peerInfoPropertyUtil.getPeerInfoMap().size());
		// System.out.println("peerCompleteMap.size()="+peerCompleteMap.size());
		if (peerInfoPropertyUtil.getPeerInfoMap().size() == peerCompleteMap.size()) {
			shutdown();
		}
	}

	/**
	 * shutdown server
	 */
	public void shutdown() {
		chokeUnchokeManager.destroy();
		optimisticUnchokeManager.destroy();
		logger.close();
		pieceManager.close();
		System.exit(0);
	}

	/**
	 * register peerHandler into peerHandlers list
	 * 
	 * @param peerHandler
	 */
	public synchronized void register(PeerHandler peerHandler) {
		peerHandlers.add(peerHandler);
	}

	/**
	 * generate BitFieldMessage
	 * 
	 * @return
	 */
	public synchronized Peer2PeerMessage getBitFieldMessage() {
		Peer2PeerMessage message = Peer2PeerMessage.create();

		message.setMessageType(Constants.TYPE_BITFIELD_MESSAGE);
		message.setBitFieldHandler(pieceManager.getBitField());

		return message;
	}

	/**
	 * gen download speed map
	 * 
	 * @return
	 */
	public HashMap<String, Double> getSpeed() {
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

		Peer2PeerMessage chokeMessage = Peer2PeerMessage.create();
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
		Peer2PeerMessage unChokeMessage = Peer2PeerMessage.create();
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
	 * the optimistically UnChokePeers
	 * 
	 * @param peerToBeUnChoked
	 */
	public void optimisticallyUnChokePeers(String peerToBeUnChoked) {
		Peer2PeerMessage unChokeMessage = Peer2PeerMessage.create();
		unChokeMessage.setMessageType(Constants.TYPE_UNCHOKE_MESSAGE);

		logger.info("Peer [" + peerId + "] has the optimistically unchoked neighbor [" + peerToBeUnChoked + "]");
		for (int i = 0, peerHandlersSize = peerHandlers.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = peerHandlers.get(i);
			if (!peerHandler.getPeerId().equals(peerToBeUnChoked)) {
				continue;
			}
			if (peerHandler.isHandshakeReceived()) {
				peerHandler.sendUnchokeMessage(unChokeMessage);
				break;
			} else {
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
	public synchronized void insertPiece(Peer2PeerMessage pieceMessage, String sourcePeerID) {
		pieceManager.write(pieceMessage.getIndex(), pieceMessage.getData());
		logger.info("Peer [" + instance.getPeerId() + "] has downloaded the piece [" + pieceMessage.getIndex()
				+ "] from [" + sourcePeerID + "]. Now the number of pieces it has is "
				+ (pieceManager.getBitField().getNoOfPieces()));
	}

	/**
	 * generate PieceMessage
	 * 
	 * @param index
	 * @return
	 */
	public Peer2PeerMessage genPieceMessage(int index) {
		Piece piece = pieceManager.get(index);
		if (piece != null) {
			Peer2PeerMessage message = Peer2PeerMessage.create();
			message.setData(piece);
			message.setIndex(index);
			message.setMessageType(Constants.TYPE_PIECE_MESSAGE);
			return message;
		}
		return null;
	}

	/**
	 * send HaveMessage
	 * 
	 * @param pieceIndex
	 * @param fromPeerID
	 */
	public void sendHaveMessage(int pieceIndex, String fromPeerID) {
		Peer2PeerMessage haveMessage = Peer2PeerMessage.create();
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
	 * broadcast shutdown message
	 */
	public void broadcastShutdown() {
		if (!isConnection() || !peerServer.isServerCompleted()) {
			return;
		}

		// shutdown message
		Peer2PeerMessage shutdownMessage = Peer2PeerMessage.create();
		shutdownMessage.setMessageType(Constants.SHUTDOWN_MESSAGE);

		// file download
		markFileDownloadComplete(peerId);
		for (int i = 0, peerHandlersSize = peerHandlers.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = peerHandlers.get(i);
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	/**
	 *
	 * @return
	 */
	public int supposedToBeConnectedCount() {
		HashMap<String, PeerInfo> neighborPeerMap = peerInfoPropertyUtil.getPeerInfoMap();
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

	public synchronized MessageLoggerUtil getLogger() {
		return logger;
	}

	public boolean isConnection() {
		return connectionEstablished;
	}

	public boolean isDownloadComplete() {
		return pieceManager.hasDownloadFileComplete();
	}

}
