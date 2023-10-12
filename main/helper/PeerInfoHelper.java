package main.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import main.constants.Constants;
import main.messageTypes.PeerInfo;

public class PeerInfoHelper {

	private LinkedHashMap<String, PeerInfo> peerInfoMap = null;
	private static PeerInfoHelper instance = null;

	public static PeerInfoHelper returnSingletonInstance() {
		if (instance == null) {
			instance = new PeerInfoHelper();
			instance.configPeerInfo();
		}
		return instance;
	}

	public boolean configPeerInfo() {
		peerInfoMap = new LinkedHashMap<>();
		try {
			FileInputStream fir = new FileInputStream(Constants.PEER_INFO_FILE);
			BufferedReader br = new BufferedReader(new InputStreamReader(fir));

			String row;
			for (; (row = br.readLine()) != null;) {
				row = row.trim();
				String[] tokens = row.split(" ");
				peerInfoMap.put(tokens[0], new PeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
			}

			br.close();
			fir.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public HashMap<String, PeerInfo> getPeerInfoMap() {
		return peerInfoMap;
	}

	public PeerInfo getPeerObjectByKey(String key) {
		return this.peerInfoMap.get(key);
	}
}
