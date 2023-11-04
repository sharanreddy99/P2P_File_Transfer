package main.constants;

/**
 * This class contains all the constants for the application
 * 
 * @author Sharan Sai Reddy Konda
 * @author Bhavan Voram
 * @author Adithya KNG
 */
public class Constants {
	// RemotePeers
	public static final int SSH_TIMEOUT = 5000; // SSH timeout in milliseconds
	public static final boolean IS_LOCAL_HOST = true; // A boolean indicating if the host is local

	// Logger Configuration
	public static final String LOG_FILE_DIRECTORY_NAME = "log";
	public static final String LOG_FILE_NAME_PREFIX = "log_peer_";

	// LogMessageFormats
	public static final String CHOKE_UNCHOKE_LOG_MESSAGE = "Peer [%s] has the preferred neighbors %s";
	public static final String FILE_DOWNLOADED_LOG_MESSAGE = "Peer [%s] has downloaded the complete file";
	public static final String OPTIMISTICALLY_UNCHOKE_LOG_MESSAGE = "Peer [%s] has the optimistically unchoked neighbor [%s]";

	// Common Config
	public static final String LOGGER_NAME = "logger.name";
	public static final String CONFIGURATION_FILE = "Common.cfg";
	public static final String PEER_INFO_FILE = "PeerInfo.cfg";
	public static final String CHOKE_UNCHOKE_INTERVAL_LABEL = "UnchokingInterval";
	public static final String OPTIMISTIC_UNCHOKE_INTERVAL_LABEL = "OptimisticUnchokingInterval";
	public static final String FILE_SIZE_LABEL = "FileSize";
	public static final String PIECE_SIZE_LABEL = "PieceSize";
	public static final String FILE_NAME_LABEL = "FileName";
	public static final String INPUT_FOLDER = "peer_%s";

	// Handshake
	public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ";

	// Pieces
	public static final int MAX_PIECES_LIMIT = 1000;

	// message id
	public static final byte TYPE_HANDSHAKE_MESSAGE = 10;
	public static final byte TYPE_CHOKE_MESSAGE = 0;
	public static final byte TYPE_UNCHOKE_MESSAGE = 1;
	public static final byte TYPE_INTERESTED_MESSAGE = 2;
	public static final byte TYPE_NOT_INTERESTED_MESSAGE = 3;
	public static final byte TYPE_HAVE_MESSAGE = 4;
	public static final byte TYPE_BITFIELD_MESSAGE = 5;
	public static final byte TYPE_REQUEST_MESSAGE = 6;
	public static final byte TYPE_PIECE_MESSAGE = 7;

	// TODO
	public static final int RAW_DATA_SIZE = 1000;
	public static final int MAX_MESSAGE_SIZE = 40000;
	public static final int SIZE_OF_EMPTY_MESSAGE = 1;
	public static final byte SHUTDOWN_MESSAGE = 100;
	public static final int SENDER_QUEUE_SIZE = 100;

}
