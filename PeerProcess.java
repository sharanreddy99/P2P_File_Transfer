import main.PeerController;

/**
 * test
 */
public class PeerProcess {
	public static void main(String args[]) {
		String peerID = args[0];

		PeerController controller = PeerController.returnSingletonInstance(peerID);
		controller.beginPeerProcess();

	}

}
