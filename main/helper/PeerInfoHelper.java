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
 * file.
 */
public class PeerInfoHelper {

    private LinkedHashMap<String, Peer> peerInfoMap = null;
    private static PeerInfoHelper instance = null;

    /**
     * This function returns a singleton PeerInfoHelper instance which has all the
     * peer info extracted from the config file.
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
     * This function extracts peer configuration info from the specified peer config
     * file.
     * 
     * @return boolean indicating whether the extraction of peer info was successful
     *         or not
     */
    public boolean configPeerInfo() {
        peerInfoMap = new LinkedHashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(Constants.PEER_INFO_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            String row;
            while ((row = bufferedReader.readLine()) != null) {
                row = row.trim();
                String[] tokens = row.split(" ");
                peerInfoMap.put(tokens[0], new Peer(tokens[0], tokens[1], tokens[2], tokens[3]));
                System.out.println(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3]);
            }

            bufferedReader.close();
            fileInputStream.close();
        } catch (IOException e) {
            System.out.printf("Exception occurred when extracting info from the peer config file. Message: %s",
                    e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Returns the complete peer info as a map.
     * 
     * @return a map of peerID to peer objects
     */
    public HashMap<String, Peer> getPeerMap() {
        return peerInfoMap;
    }

    /**
     * Returns a single peer object based on the peerID.
     * 
     * @param key The peerID to retrieve the peer object.
     * @return a single peer object or null if not found
     */
    public Peer getPeerObjectByKey(String key) {
        return this.peerInfoMap.get(key);
    }
}
