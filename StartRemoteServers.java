import main.constants.Constants;
import main.helper.OutputDisplayUtil;
import main.helper.PeerInfoHelper;
import main.message.meta.PeerInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * process start
 */
public class StartRemoteServers {
	String path = "cn_project";
	String runCommand = "rm *.class && javac PeerProcess.java && javac ProcessStarter.java && java PeerProcess";

	public static void main(String args[]) throws Exception {
		StartRemoteServers starter = new StartRemoteServers();
		starter.remoteStartProcesses();
	}

	public void remoteStartProcesses() throws Exception {

		// Fetch the peer info configuration from the config file.
		PeerInfoHelper fileReader = PeerInfoHelper.getInstance();

		HashMap<String, PeerInfo> peerMap = fileReader.getPeerInfoMap();

		for (String key : peerMap.keySet()) {
			PeerInfo peer = peerMap.get(key);

			// Prepare execution strings.
			String formatString = (Constants.IS_LOCAL_HOST ? "java PeerProcess %s"
					: "ssh %s@%s cd %s && %s %s");
			String execCommand = (Constants.IS_LOCAL_HOST ? String.format(formatString, peer.getPeerId())
					: String.format("ssh %s@%s cd %s && %s %s", "kondas", peer.getAddress(), path,
							runCommand, peer.getPeerId()));

			// Run the command to start peers locally or remotely.
			Process serverProcess = Runtime.getRuntime().exec(execCommand);
			OutputDisplayUtil outputDisplayUtil = new OutputDisplayUtil(peer.getPeerId(),
					new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));

			new Thread(outputDisplayUtil).start();

			OutputDisplayUtil errorDisplayer = new OutputDisplayUtil(peer.getPeerId(),
					new BufferedReader(new InputStreamReader(serverProcess.getErrorStream())));
			new Thread(errorDisplayer).start();

			Thread.sleep(Constants.SSH_TIMEOUT);

			System.out.println(String.format("Started process for Peer: %s at host: %s on Port: %s", peer.getPeerId(),
					peer.getAddress(), peer.getPort()));
		}
	}
}
