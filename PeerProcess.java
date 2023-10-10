import main.Controller;

/**
 * test
 */
public class PeerProcess {
	public static void main(String args[]) {
		String peerID = args[0];

		Controller controller = Controller.getInstance(peerID);
		controller.startController();

	}

}
