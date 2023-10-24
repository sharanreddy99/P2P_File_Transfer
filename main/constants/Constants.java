package main.constants;

public class Constants {
    // RemotePeers
    public static final int SSH_TIMEOUT = 5000; // SSH timeout in milliseconds
    public static final boolean IS_LOCAL_HOST = true; // A boolean indicating if the host is local

    // Message types
    public static final byte TYPE_HANDSHAKE_MESSAGE = 0; // Type identifier for handshake messages

    // Queue Size
    public static final int SENDER_QUEUE_SIZE = 100; // Size of the message sender queue

    /* Log related constants */
    public static final String LOG_DIRECTORY_NAME = "log"; // Name of the log directory
    public static final String LOG_FILE_PREFIX = "log_peer_"; // Prefix for log file names

    /* Handshake message header */
    public static final String HANDSHAKE_HEADER_STRING = "P2PFILESHARINGPROJ"; // Header for handshake messages

    /* Input file names*/
    public static final String CONFIGURATION_FILE = "Common.cfg"; // Configuration file name
    public static final String LOGGER_NAME = "logger.name"; // Name of the logger
    public static final String FILE_SIZE = "FileSize"; // File size configuration key
}
