package main.helper;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import main.PeerController;
import main.constants.Constants;

/**
 * This class runs the process of unchoking an optimistically chosen random
 * choked peer.
 * 
 * @author Sharan Sai Reddy Konda
 * @author Bhavan Voram
 */
public class OptimisticUnchokePeerHelper implements Runnable {
	private static OptimisticUnchokePeerHelper instance = null;
	private PeerController controller = null;
	private LogHelper logger = null;

	private ScheduledFuture<?> process = null;

	/**
	 * Returns the singleton instance of the optimistic unchoke helper
	 * 
	 * @param controller - main controller object that manages all other objects
	 * @return null
	 */
	public static synchronized OptimisticUnchokePeerHelper returnSingletonInstance(PeerController controller) {
		if (instance == null) {

			if (controller == null) {
				return null;
			}

			instance = new OptimisticUnchokePeerHelper();
			instance.controller = controller;
			instance.logger = controller.getLogger();
		}

		return instance;
	}

	/**
	 * This function randomly finds one choked peer from the given list of choked
	 * nodes and unchokes it.
	 * 
	 * @return null
	 */
	public void run() {
		ArrayList<String> chokedPeers = controller.getChokedPeers();
		if (chokedPeers.size() > 0) {
			int randomPeer = new Random().nextInt(chokedPeers.size());
			controller.optimisticallyUnChokePeers(chokedPeers.get(randomPeer));
		}

		controller.updateFileDownloadStatus();
		if (controller.isFileDownloadComplete()) {
			logger.logMessage(String.format(Constants.FILE_COMPLETE_DOWNLOAD_LOG_MESSAGE, controller.getPeerId()));
			controller.broadcastShutdown();
		}
	}

	/**
	 * Repeatedly execute the choking and unchoking process for the given peer at
	 * intervals extracted from the config file.
	 * 
	 * @param startDelay    - indicates the starting delay because the repetitve
	 *                      process is triggered
	 * @param intervalDelay - indicates the interval at which the choking and
	 *                      unchoking process should execute
	 * @return null
	 */
	public void start(int startDelay, int intervalDelay) {
		process = Executors.newScheduledThreadPool(5).scheduleAtFixedRate(this, startDelay, intervalDelay,
				TimeUnit.SECONDS);
	}

	/**
	 * Cancels the repetitive process either due to completion of peer downloads or
	 * any
	 * other reason.
	 * 
	 * @return null
	 */
	public void destroy() {
		process.cancel(true);
	}
}
