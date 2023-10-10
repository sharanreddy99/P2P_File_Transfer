package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import main.helper.PeerInfoHelper;
import main.manager.peerhandler.PeerHandler;
import main.messageTypes.PeerInfo;

/**
 * PeerServer
 */
public class PeerServer implements Runnable {

	private PeerInfoHelper peerConfigurationReader; // PeerInfo.cfg data
	private String peerServerID; // peer server id
	private Controller controller;
	private ServerSocket serverSocket;

	private boolean serverCompleted = false;

	private static volatile PeerServer instance = null;

	/**
	 * getInstance
	 * 
	 * @param peerServerID
	 * @param controller
	 * @return
	 */
	public static PeerServer getInstance(String peerServerID, Controller controller) {
		if (instance == null) {
			instance = new PeerServer();
			instance.peerServerID = peerServerID;
			instance.controller = controller;
			if (!instance.init(controller)) {
				instance = null;
			}
		}
		return instance;
	}

	/**
	 * init
	 * 
	 * @param controller
	 * @return
	 */
	public boolean init(Controller controller) {
		peerConfigurationReader = PeerInfoHelper.getInstance();
		return peerConfigurationReader != null;
	}

	/**
	 * run server
	 */
	public void run() {
		HashMap<String, PeerInfo> peerInfoMap = peerConfigurationReader.getPeerInfoMap();
		PeerInfo serverPeerInfo = peerInfoMap.get(peerServerID);
		int peerServerPortNumber = serverPeerInfo.getPort();
		try {
			serverSocket = new ServerSocket(peerServerPortNumber);
			int count = controller.supposedToBeConnectedCount();
			for (int i = 0; i < count; i++) {
				Socket socketTmp = serverSocket.accept();
				PeerHandler peerHandler = PeerHandler.getNewInstance(socketTmp, controller);

				controller.register(peerHandler);
				new Thread(peerHandler).start();
			}

			setServerCompleted(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean isServerCompleted() {
		return serverCompleted;
	}

	public synchronized void setServerCompleted(boolean isPeerServerCompleted) {
		this.serverCompleted = isPeerServerCompleted;
	}

}