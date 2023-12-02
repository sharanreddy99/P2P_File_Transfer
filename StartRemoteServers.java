import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;

import main.constants.Constants;
import main.helper.PeerInfoHelper;
import main.messageTypes.Peer;

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

    // starts the execution of peerProcesses on each of the mentioned peers.
    public void remoteStartProcesses() throws Exception {

        // Fetch the peer configuration from the config file.
        PeerInfoHelper fileReader = PeerInfoHelper.returnSingletonInstance();
        HashMap<String, Peer> peerMap = fileReader.getPeerMap();

        if (Constants.IS_LOCAL_HOST) {
            Runtime.getRuntime().exec("make");
            Thread.sleep(Constants.SSH_TIMEOUT);
        }

        // Copy code to remote server
        for (String key : peerMap.keySet()) {
            Peer peer = peerMap.get(key);

            // Prepare execution strings.
            String formatString = (Constants.IS_LOCAL_HOST ? "%s%s"
                    : "scp -r ../%s %s@%s:~/");

            String execCommand = (Constants.IS_LOCAL_HOST
                    ? String.format(formatString, "make runPeer peerid=", peer.getPeerId())
                    : String.format(formatString, path, userName, peer.getAddress()));

            // Run the command to start peers locally or remotely.
            Process serverProcess = Runtime.getRuntime()
                    .exec(execCommand);

            DisplayHelper stdOut = new DisplayHelper(peer.getPeerId(),
                    new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));

            DisplayHelper stdErr = new DisplayHelper(peer.getPeerId(),
                    new BufferedReader(new InputStreamReader(serverProcess.getErrorStream())));

            new Thread(stdOut).start();
            new Thread(stdErr).start();
            Thread.sleep(Constants.SSH_TIMEOUT);
        }

        // Execute code in remote server
        for (String key : peerMap.keySet()) {
            Peer peer = peerMap.get(key);

            // Prepare execution strings.
            String formatString = (Constants.IS_LOCAL_HOST ? "%s%s"
                    : "ssh %s@%s cd %s && %s%s");

            String execCommand = (Constants.IS_LOCAL_HOST
                    ? String.format(formatString, "make runPeer peerid=", peer.getPeerId())
                    : String.format(formatString, userName, peer.getAddress(),
                            path,
                            runCommand, peer.getPeerId()));

            // Run the command to start peers locally or remotely.
            Process serverProcess = Runtime.getRuntime()
                    .exec(execCommand);

            DisplayHelper stdOut = new DisplayHelper(peer.getPeerId(),
                    new BufferedReader(new InputStreamReader(serverProcess.getInputStream())));

            DisplayHelper stdErr = new DisplayHelper(peer.getPeerId(),
                    new BufferedReader(new InputStreamReader(serverProcess.getErrorStream())));

            new Thread(stdOut).start();
            new Thread(stdErr).start();
            Thread.sleep(Constants.SSH_TIMEOUT);

            System.out.println(String.format("Started process for Peer: %s at host: %s on Port: %s", peer.getPeerId(),
                    peer.getAddress(), peer.getPort()));
        }
    }
}

class DisplayHelper implements Runnable {
    BufferedReader br;
    String peerID;

    /**
     * Constructs a DisplayHelper instance for displaying messages.
     *
     * @param peerID The identifier of the peer for which messages are displayed.
     * @param br     The BufferedReader to read messages from.
     */
    public DisplayHelper(String peerID, BufferedReader br) {
        this.peerID = peerID;
        this.br = br;
    }

    /**
     * Runs the DisplayHelper to continuously read and display messages.
     */
    public void run() {
        try {
            String row;
            for (; (row = br.readLine()) != null;) {
                System.out.printf("Message for Peer %s: %s\n", peerID, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
