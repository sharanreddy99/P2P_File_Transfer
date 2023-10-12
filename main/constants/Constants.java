package main.constants;

public class Constants {
	// RemotePeers
	public static final int SSH_TIMEOUT = 5000;
	public static final boolean IS_LOCAL_HOST = true;

	// LogMessageFormats
	public static final String CHOKE_UNCHOKE_LOG_MESSAGE = "Peer [%s] has the preferred neighbors %s";

	public static final int RAW_DATA_SIZE = 1000;
	public static final int MAX_MESSAGE_SIZE = 40000;

	// message id
	public static final byte TYPE_CHOKE_MESSAGE = 0;
	public static final byte TYPE_UNCHOKE_MESSAGE = 1;
	public static final byte TYPE_INTERESTED_MESSAGE = 2;
	public static final byte TYPE_NOT_INTERESTED_MESSAGE = 3;
	public static final byte TYPE_HAVE_MESSAGE = 4;
	public static final byte TYPE_BITFIELD_MESSAGE = 5;
	public static final byte TYPE_REQUEST_MESSAGE = 6;
	public static final byte TYPE_PIECE_MESSAGE = 7;
	public static final byte TYPE_HANDSHAKE_MESSAGE = 9;

	public static final int SIZE_OF_EMPTY_MESSAGE = 1;
	public static final byte SHUTDOWN_MESSAGE = 100;

	public static final int SENDER_QUEUE_SIZE = 100;

	/* log directory and name */
	public static final String LOG_FILE_DIRECTORY_NAME = "log";
	public static final String LOG_FILE_NAME_PREFIX = "log_peer_";

	/* handshake header */
	public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ";

	/* config logMessage( */
	public static final String LOGGER_NAME = "logger.name";
	public static final String CONFIGURATION_FILE = "Common.cfg";
	public static final String PEER_INFO_FILE = "PeerInfo.cfg";
	public static final String CHOKE_UNCHOKE_INTERVAL = "UnchokingInterval";
	public static final String OPTIMISTIC_UNCHOKE_INTERVAL = "OptimisticUnchokingInterval";
	public static final String FILE_SIZE = "FileSize";

	/**
	 * getMessage
	 * 
	 * @param i
	 * @return
	 */
	public static String getMessageName(int i) {
		switch (i) {
			case Constants.TYPE_BITFIELD_MESSAGE:
				return "BITFIELD_MESSAGE";
			case Constants.TYPE_REQUEST_MESSAGE:
				return "REQUEST_MESSAGE";
			case Constants.TYPE_HANDSHAKE_MESSAGE:
				return "HANDSHAKE_MESSAGE";
			case Constants.TYPE_CHOKE_MESSAGE:
				return "CHOKE_MESSAGE";
			case Constants.TYPE_UNCHOKE_MESSAGE:
				return "UNCHOKE_MESSAGE";
			case Constants.TYPE_HAVE_MESSAGE:
				return "HAVE_MESSAGE";
			case Constants.TYPE_INTERESTED_MESSAGE:
				return "INTERESTED_MESSAGE";
			case Constants.TYPE_NOT_INTERESTED_MESSAGE:
				return "NOT_INTERESTED_MESSAGE";
			case Constants.TYPE_PIECE_MESSAGE:
				return "PIECE_MESSAGE";
			case Constants.SHUTDOWN_MESSAGE:
				return "SHUTDOWN_MESSAGE";
		}

		return null;
	}
}
