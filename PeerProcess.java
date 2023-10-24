import main.PeerController;

/**
 * Main class to start the PeerProcess. The peer ID is given as a command line argument.
 */
public class PeerProcess {
    public static void main(String args[]) {
        PeerController controller = PeerController.returnSingletonInstance(args[0]);
        controller.beginPeerProcess();
    }
}
