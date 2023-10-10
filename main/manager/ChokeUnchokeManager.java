package main.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import main.Controller;
import main.helper.AsyncUtil;
import main.helper.CommonPropertyUtil;
import main.helper.MessageLoggerUtil;

/**
 * ChokeUnchokeManager
 */
@SuppressWarnings("unchecked")
public class ChokeUnchokeManager implements Runnable {

	private Controller controller = null;
	private MessageLoggerUtil logger = null;

	private static volatile ChokeUnchokeManager instance = null; // static instance

	/* task */
	private ScheduledFuture<?> task = null;

	/**
	 * get instance
	 * 
	 * @param controller
	 * @return
	 */
	public static synchronized ChokeUnchokeManager getInstance(Controller controller) {
		if (instance == null) {
			if (controller == null) {
				return null;
			}

			instance = new ChokeUnchokeManager();
			instance.logger = controller.getLogger();
			instance.controller = controller;
		}

		return instance;
	}

	public void destroy() {
		// System.out.println(LOGGER_PREFIX + " Shutting down
		// ChokeUnchokeManager......");
		task.cancel(true);
	}

	public void run() {
		int preferredNeighbors = 0;
		HashMap<String, Double> speedMap = controller.getSpeed();

		if (CommonPropertyUtil.getProperty("NumberOfPreferredNeighbors") != null) {
			preferredNeighbors = Integer.parseInt(CommonPropertyUtil.getProperty("NumberOfPreferredNeighbors"));
		}

		if (preferredNeighbors > speedMap.size()) {
			// System.err.println("ChokeUnchokeManager : Number of preferred neighbors is
			// more than total peers. Might be problem. ");
		} else {
			ArrayList<String> unchokePeers = new ArrayList<String>();
			// Find top k preferred neighbours

			// creating a LinkedHashMap sorted on values for selecting top k
			// preferred neighbors
			Set<Entry<String, Double>> entrySet = speedMap.entrySet();

			Entry<String, Double>[] tempArr = new Entry[speedMap.size()];
			tempArr = entrySet.toArray(tempArr);

			for (int i = 0; i < tempArr.length; i++) {
				for (int j = i + 1; j < tempArr.length; j++) {
					if (tempArr[i].getValue().compareTo(tempArr[j].getValue()) == -1) {
						Entry<String, Double> tempEntry = tempArr[i];
						tempArr[i] = tempArr[j];
						tempArr[j] = tempEntry;
					}
				}
			}

			// To make valuecomparator object working.
			LinkedHashMap<String, Double> sortedSpeedMap = new LinkedHashMap<String, Double>();

			// System.out.print(LOGGER_PREFIX + " Peer Speed : ");
			for (int i = 0; i < tempArr.length; i++) {
				sortedSpeedMap.put(tempArr[i].getKey(), tempArr[i].getValue());
				// System.out.print(tempArr[i].getKey() + ":[" + tempArr[i].getValue() + "] " +
				// " , ");
			}
			// System.out.println(" ");

			int count = 0;

			// adding preferredNeighbors string to ArrayList

			for (Entry<String, Double> entry : sortedSpeedMap.entrySet()) {
				String key = entry.getKey();
				unchokePeers.add(key);
				count++; // maintaining count to break out of map iterator
				if (count == preferredNeighbors)
					break;
			}

			ArrayList<String> chokedPeerList = new ArrayList<String>();

			for (String peerID : unchokePeers) {
				sortedSpeedMap.remove(peerID);
			}
			chokedPeerList.addAll(sortedSpeedMap.keySet());

			// System.out.print(LOGGER_PREFIX + ": Choking these peers: ");

			for (String peerID : chokedPeerList) {
				// System.out.print(peerID + " , ");
			}

			// System.out.println(" ");
			// System.out.print(LOGGER_PREFIX + ": Unchoking these peers: ");

			String logMessage = "Peer [" + controller.getPeerId() + "] has the preferred neighbors [";

			for (String peerID : unchokePeers) {
				// System.out.print(peerID + " , ");
				logMessage += peerID + " , ";
			}
			// System.out.println(" ");

			logMessage += "]";

			logger.info(logMessage);

			controller.unChokePeers(unchokePeers);
			controller.setChokePeers(chokedPeerList);
		}
	}

	// delay iin seconds
	public void start(int startDelay, int intervalDelay) {
		task = AsyncUtil.submit(this, startDelay, intervalDelay);
	}

}
