package main.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import main.PeerController;
import main.constants.Constants;

/**
 * This class runs the process of choking an unchoking at regular intervals for
 * the given peer in order to change its neighboring peers.
 * 
 * @author Sharan Sai Reddy Konda
 */
public class ChokeUnchokePeerHelper implements Runnable {

	private LogHelper logger;
	private PeerController controller;
	private static ChokeUnchokePeerHelper instance = null; // static instance

	private ScheduledFuture<?> process = null;

	/**
	 * Returns the singleton instance of the choke unchoke helper
	 * 
	 * @param controller - main controller object that manages all other objects
	 * @return null
	 */
	public static synchronized ChokeUnchokePeerHelper returnSingletonInstance(PeerController controller) {
		if (instance == null) {

			if (controller == null) {
				return null;
			}

			instance = new ChokeUnchokePeerHelper();
			instance.controller = controller;
			instance.logger = controller.getLogger();
		}

		return instance;
	}

	/**
	 * This function calculates the download rates for each peer and picks the first
	 * k neighbors based on highest download rate speeds and chokes the rest of the
	 * peers.
	 * 
	 * @return null
	 */
	public void run() {
		HashMap<String, Double> peerSpeedMap = controller.getDownloadRates();

		int preferredNeighbors = 0;
		if (CommonConfigHelper.getConfig("NumberOfPreferredNeighbors") != null) {
			preferredNeighbors = Integer.parseInt(CommonConfigHelper.getConfig("NumberOfPreferredNeighbors"));
		}

		// If we lesser peers than the actual peers count, then we have to pick the k
		// best peers and choke the rest.
		if (preferredNeighbors <= peerSpeedMap.size()) {

			// Find top k preferred neighbours based on their download speeds
			List<Map.Entry<String, Double>> peerSpeedList = new ArrayList<>(peerSpeedMap.entrySet());

			// Sort in descending order based on speeds
			Collections.sort(peerSpeedList, (peer1, peer2) -> peer2.getValue().compareTo(peer1.getValue()));

			ArrayList<String> unchokePeersList = new ArrayList<String>();
			ArrayList<String> chokePeersList = new ArrayList<String>();

			// Pick the first k preferred neighbors from the sorted list and choke the rest
			// of the neighbors.
			int count = 0;
			for (Map.Entry<String, Double> peer : peerSpeedList) {
				if (count < preferredNeighbors) {
					unchokePeersList.add(peer.getKey());
				} else {
					chokePeersList.add(peer.getKey());
				}

				count++;
			}

			String logMessage = String.format(Constants.CHOKE_UNCHOKE_LOG_MESSAGE, controller.getPeerId(),
					String.join(",", unchokePeersList));

			logger.logMessage(logMessage);
			controller.unChokePeers(unchokePeersList);
			controller.setChokePeers(chokePeersList);
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
	 * any other reason.
	 * 
	 * @return null
	 */
	public void destroy() {
		process.cancel(true);
	}
}
