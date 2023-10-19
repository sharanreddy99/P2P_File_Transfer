import main.PeerController;
/*
 * Start PeerProcess, peer id given in common.cfg file is given as command line argument
 */
public class PeerProcess {
	public static void main(String args[]) {
		PeerController controller = PeerController.returnSingletonInstance(args[0]);
		controller.beginPeerProcess();
	}
}
