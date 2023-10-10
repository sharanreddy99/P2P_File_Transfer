import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import main.constants.Constants;
import main.helper.OutputDisplayUtil;
import main.helper.PeerInfoHelper;
import main.message.meta.PeerInfo;

/**
 * StartRemoteServers: This class starts the peering processes in each of the
 * specified peers (either locally or remotely)
 * 
 * @author Sharan Sai Reddy Konda
 */
public class StartRemoteServers {
	String path = "cn_project";
	String runCommand = "make && make runPeer peerid=";
	String userName = "kondas";

	public static void main(String args[]) throws Exception {
		StartRemoteServers starter = new StartRemoteServers();
		starter.remoteStartProcesses();
	}

	// starts the execution of peerProcesses on each of the mentioend peers.
	public void remoteStartProcesses() throws Exception {

		// Fetch the peer info configuration from the config file.
		PeerInfoHelper fileReader = PeerInfoHelper.getInstance();
		HashMap<String, PeerInfo> peerMap = fileReader.getPeerInfoMap();

		if (Constants.IS_LOCAL_HOST) {
			Runtime.getRuntime().exec("make");
			Thread.sleep(Constants.SSH_TIMEOUT);
		}

		for (String key : peerMap.keySet()) {
			PeerInfo peer = peerMap.get(key);

			// Prepare execution strings.
			String formatString = (Constants.IS_LOCAL_HOST ? "%s%s"
					: "ssh %s@%s cd %s && %s%s");

			String execCommand = (Constants.IS_LOCAL_HOST
					? String.format(formatString, "make runPeer peerid=", peer.getPeerId())
					: String.format(formatString, userName, peer.getAddress(), path,
							runCommand, peer.getPeerId()));

			// Run the command to start peers locally or remotely.
			Runtime.getRuntime().exec(execCommand);
			Thread.sleep(Constants.SSH_TIMEOUT);

			System.out.println(String.format("Started process for Peer: %s at host: %s on Port: %s", peer.getPeerId(),
					peer.getAddress(), peer.getPort()));
		}
	}
}
