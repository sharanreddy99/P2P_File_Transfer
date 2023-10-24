package main.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import main.constants.Constants;
import main.messageTypes.Peer;

/**
 * This class extracts information related to all the peers from the peer config
 * file
 */
public class PeerInfoHelper {

	private LinkedHashMap<String, Peer> peerInfoMap = null;
	private static PeerInfoHelper instance = null;

	/**
	 * This function returns a singleton peerInfo helper instance which
	 * has the all the peer info extracted from the config file.
	 * 
	 * @return PeerInfoHelper
	 */
	public static PeerInfoHelper returnSingletonInstance() {
		if (instance == null) {

			instance = new PeerInfoHelper();
			instance.configPeerInfo();
		}

		return instance;
	}

	/**
	 * This function extracts config info from the mentioned peer config file
	 * 
	 * @return boolean indicating whether the extraction of peer info was successful
	 *         or not
	 */
	public boolean configPeerInfo() {
		peerInfoMap = new LinkedHashMap<>();
		try {
			FileInputStream fir = new FileInputStream("PeerInfo.cfg");
			BufferedReader br = new BufferedReader(new InputStreamReader(fir));

			String row;
			for (; (row = br.readLine()) != null;) {
				row = row.trim();
				String[] tokens = row.split(" ");
				peerInfoMap.put(tokens[0], new Peer(tokens[0], tokens[1], tokens[2], tokens[3]));
				System.out.println(tokens[0]+" "+tokens[1]+" "+tokens[2]+" "+tokens[3]);
			}

			br.close();
			fir.close();
		} catch (IOException e) {
			System.out.printf("Exception occured when extracting info from the common config file. Message: %s",
					e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Returns the complete peer info
	 * 
	 * @return a map of peerID, peer Object
	 */
	public HashMap<String, Peer> getPeerMap() {
		return peerInfoMap;
	}

	/**
	 * returns the single peer info object based on peerID
	 * 
	 * @return a single peer object
	 */
	public Peer getPeerObjectByKey(String key) {
		return this.peerInfoMap.get(key);
	}
}
