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
	public static final int SSH_TIMEOUT = 2500; // SSH timeout in milliseconds
	public static final boolean IS_LOCAL_HOST = true; // A boolean indicating if the host is local

	// Logger Configuration
	public static final String LOG_FILE_DIRECTORY_NAME = "log";
	public static final String LOG_FILE_NAME_PREFIX = "log_peer_";
	public static final boolean SHOW_OPTIONAL_LOG_MESSAGES = true;

	// LogMessageFormats
	public static final String MAKE_CONNECTION_SENDER_LOG_MESSAGE = "Peer [%s] makes a connection to Peer [%s]";
	public static final String MAKE_CONNECTION_RECEIVER_LOG_MESSAGE = "Peer [%s] is connected from Peer [%s]";
	public static final String CHANGE_OF_PREFERRED_NEIGHBORS_LOG_MESSAGE = "Peer [%s] has the preferred neighbors [%s]";
	public static final String CHANGE_OF_OPTIMISTICALLY_UNCHOKED_NEIGHBORS_LOG_MESSAGE = "Peer [%s] has the optimistically unchoked neighbor [%s]";
	public static final String UNCHOKED_LOG_MESSAGE = "Peer [%s] is unchoked by [%s]";
	public static final String CHOKED_LOG_MESSAGE = "Peer [%s] is choked by [%s]";
	public static final String HAVE_LOG_MESSAGE = "Peer [%s] received the 'have' message from [%s] for the piece [%s]";
	public static final String INTERESTED_LOG_MESSAGE = "Peer [%s] received the 'interested' message from [%s]";
	public static final String NOT_INTERESTED_LOG_MESSAGE = "Peer [%s] received the 'not interested' message from [%s]";
	public static final String FILE_PARTIAL_DOWNLOADE_LOG_MESSAGE = "Peer [%s] has downloaded the piece [%s] from [%s]. Now the number of pieces it has is [%s]";
	public static final String FILE_COMPLETE_DOWNLOAD_LOG_MESSAGE = "Peer [%s] has downloaded the complete file";

	// OptionalLogMessageFormats
	public static final String SENDER_HANDSHAKE_LOG_MESSAGE = "Peer [%s] has sent a handshake message to Peer [%s] with the header [%s]";
	public static final String BITFIELD_LOG_MESSAGE = "Peer [%s] has received from Peer [%s] the following bitfields set [%s]";
	public static final String HANDSHAKE_FAILED_LOG_MESSAGE = "Handshake failed with Peer [%s] due to incorrect header [%s]";

	// Common Config
	public static final String LOGGER_NAME = "logger.name";
	public static final String CONFIGURATION_FILE = "Common.cfg";
	public static final String PEER_INFO_FILE = "PeerInfo.cfg";
	public static final String PREFERRED_NEIGHBORS_LABEL = "NumberOfPreferredNeighbors";
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
	public static final byte TYPE_SHUTDOWN_MESSAGE = 100;

	// TODO
	public static final int RAW_DATA_SIZE = 1000;
	public static final int MAX_MESSAGE_SIZE = 40000;
	public static final int SIZE_OF_EMPTY_MESSAGE = 1;
	public static final int SENDER_QUEUE_SIZE = 100;

}
