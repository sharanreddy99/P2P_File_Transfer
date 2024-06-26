import main.PeerController;

/**
 * Main class to start the PeerProcess. The peer ID is given as a command line
 * argument.
 * 
 * @author Bhavan Voram
 */
public class PeerProcess {
	public static void main(String args[]) {
		String peerID = args[0];

		PeerController controller = PeerController.returnSingletonInstance(peerID);
		controller.beginPeerProcess();

	}

}
