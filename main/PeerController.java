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
 * Peer Controller class is responsible for managing the peer's operations, connections, and data segments.
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

    /**
     * Private constructor for the PeerController class.
     */
    private PeerController(String peerId) {
        this.peerId = peerId;
    }

    /**
     * Return a singleton instance of the PeerController for the given peer.
     *
     * @param peerID The ID of the peer
     * @return The singleton PeerController instance
     */
    public static synchronized PeerController returnSingletonInstance(String peerID) {
        if (instance != null) {
            return instance;
        } else {
            instance = new PeerController(peerID);
            if (!instance.configController()) {
                instance = null;
            }
        }
        return instance;
    }

    /**
     * Start the peer process, including the peer server and connecting to other peers.
     */
    public void beginPeerProcess() {
        // Start the current peer server
        new Thread(peerServer).start();

        // Connect to all previously started peers
        HashMap<String, Peer> peerInfoMap = peerInfoHelperObj.getPeerMap();

        try {
            int currentPeerId = Integer.parseInt(peerId);
            for (Map.Entry<String, Peer> set : peerInfoMap.entrySet()) {
                if (Integer.parseInt(set.getKey()) < currentPeerId) {
                    establishConnection(peerInfoMap.get(set.getKey()));
                }
            }
        } catch (Exception e) {
            System.out.printf("Exception occurred while creating connections with neighbors. Message: %s\n", e.getMessage());
            setIsConnectionEstablished(false);
            return;
        }
        setIsConnectionEstablished(true);
    }

    /**
     * Establish a connection with a peer.
     *
     * @param peer The peer to connect to
     * @throws IOException if an I/O error occurs
     */
    private void establishConnection(Peer peer) throws IOException {
        Socket neighborPeer = new Socket(peer.getAddress(), peer.getPort());
        ConnectionManager getNewPeerHandler = ConnectionManager.createNewInstance(neighborPeer, this);
        setIdAndStartPeer(getNewPeerHandler, peer);
    }

    /**
     * Configure the data segment manager based on whether the peer has the target file or not.
     *
     * @param isFileExists true if the peer has the target file, false otherwise
     */
    private void addDataSegmentManager(boolean isFileExists) {
        this.dataSegmentManager = ManageDataSegments.returnSingletonInstance(isFileExists, peerId);
    }

    /**
     * Configure the PeerController by setting up the data segment manager, logger, and peer server.
     *
     * @return true if configuration is successful, false otherwise
     */
    private boolean configController() {
        peerInfoHelperObj = PeerInfoHelper.returnSingletonInstance();
        Peer currPeer = peerInfoHelperObj.getPeerObjectByKey(peerId);
        boolean isFileExists = currPeer != null && currPeer.hasFile();
        this.connectionManagers = new ArrayList<ConnectionManager>();

        // Configure the data segment manager based on whether the peer has the target file or not
        addDataSegmentManager(isFileExists);
        if (dataSegmentManager == null) {
            return false;
        }

        // Configure the logger instance
        logger = new LogHelper(peerId);
        if (!logger.isLoggerInitialized()) {
            return false;
        }

        // Configure the peer server
        peerServer = PeerServer.returnSingletonInstance(peerId, this);

        // Configuration successful
        return true;
    }

    /**
     * Terminate all necessary objects by closing and freeing them from memory and exit the process safely.
     */
    public void terminateObjects() {
        logger.destroy();
        System.exit(0);
    }

    /**
     * Add the given peer connectionManager to the connectionManagers list.
     *
     * @param connectionManager The connection manager to add
     */
    public synchronized void addPeerHandler(ConnectionManager connectionManager) {
        connectionManagers.add(connectionManager);
    }

    /**
     * Get the maximum count of new connections to be established.
     *
     * @return The count of new connections
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
        for (int i = 0; i < peerIdList.size(); i++) {
            if (peerIdList.get(i) > id) {
                connectionCount++;
            }
        }
        return connectionCount;
    }

    /**
     * Get the ID of the peer.
     *
     * @return The peer ID
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * Get the status of whether all connections have been established.
     *
     * @return true if all connections have been established, false otherwise
     */
    public boolean getIsConnectionEstablished() {
        return isConnectionEstablished;
    }

    /**
     * Set the status of whether all connections have been established.
     *
     * @param isAllPeersConnection true if all connections have been established, false otherwise
     */
    public void setIsConnectionEstablished(boolean isAllPeersConnection) {
        this.isConnectionEstablished = isAllPeersConnection;
    }

    /**
     * Get the logger instance.
     *
     * @return The logger instance
     */
    public synchronized LogHelper getLogger() {
        return logger;
    }

    /**
     * Set the peer ID and start the peer process.
     *
     * @param peerHandler The peer connection manager
     * @param peer        The peer object
     */
    public void setIdAndStartPeer(ConnectionManager peerHandler, Peer peer) {
        peerHandler.setPeerId(peer.getPeerId());
        connectionManagers.add(peerHandler);
        new Thread(peerHandler).start();
    }
}
