package main.constants;

public class Constants {
	// RemotePeers
	public static final int SSH_TIMEOUT = 5000;
	public static final boolean IS_LOCAL_HOST = true;

	// message types
	public static final byte TYPE_HANDSHAKE_MESSAGE = 0;

	// Queue Size
	public static final int SENDER_QUEUE_SIZE = 100;

	/* Log related constants */
	public static final String LOG_DIRECTORY_NAME = "log";
	public static final String LOG_FILE_PREFIX = "log_peer_";

	/* handshake message header */
	public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ";

	/* input file names*/
	public static final String CONFIGURATION_FILE = "Common.cfg";
	public static final String LOGGER_NAME = "logger.name";
	public static final String FILE_SIZE = "FileSize";
}
