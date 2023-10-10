package main.helper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

import main.constants.Constants;

/**
 * MessageLoggerUtil
 */
public class MessageLoggerUtil extends Logger {
	private final String logFileName; // log name
	private final String peerId;// peerId

	private FileHandler fileHandler;

	private SimpleDateFormat formatter = null;

	/**
	 * construct
	 * 
	 * @param peerID
	 * @param logFileName
	 * @param name
	 */
	public MessageLoggerUtil(String peerID, String logFileName, String name) {
		super(name, null);
		this.logFileName = logFileName;
		this.setLevel(Level.FINEST);
		this.peerId = peerID;
	}

	/**
	 * init
	 * 
	 * @param peerId
	 * @return
	 */
	public static MessageLoggerUtil init(String peerId) {
		String directory = "" + Constants.LOG_FILE_DIRECTORY_NAME;
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}

		MessageLoggerUtil logger = new MessageLoggerUtil(peerId,
				directory + "/" + Constants.LOG_FILE_NAME_PREFIX + peerId + ".log", Constants.LOGGER_NAME);
		try {
			logger.init();
		} catch (Exception e) {
			e.printStackTrace();
			logger.close();
			logger = null;
			System.out.println("Unable to create or initialize logger");
		}
		return logger;

	}

	/**
	 * inner init
	 * 
	 * @throws SecurityException
	 * @throws IOException
	 */
	private void init() throws SecurityException, IOException {
		fileHandler = new FileHandler(logFileName);
		fileHandler.setFormatter(new SimpleFormatter() {
			@Override
			public synchronized String format(LogRecord record) {
				if (record != null) {
					return record.getMessage();
				} else {
					return null;
				}
			}

			@Override
			public synchronized String formatMessage(LogRecord record) {
				return this.format(record);
			}
		});

		formatter = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss a");
		this.addHandler(fileHandler);
	}

	/**
	 * close
	 */
	public void close() {
		try {
			if (fileHandler != null) {
				fileHandler.close();
			}
		} catch (Exception e) {
			System.out.println("Unable to close logger.");
			e.printStackTrace();
		}
	}

	/**
	 * print error log
	 * 
	 * @param prefix
	 * @param message
	 * @param e
	 */
	public void error(String prefix, String message, Exception e) {
		this.log(Level.SEVERE, "[" + prefix + "]: " + message);
		if (e != null) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			this.log(Level.FINEST, "[" + prefix + "]: " + e.getMessage());
			for (StackTraceElement stackTraceElement : stackTrace) {
				this.log(Level.FINEST, stackTraceElement.toString());
			}
		}
	}

	public void error(String message) {
		this.log(message, Level.SEVERE);
	}

	public void debug(String message) {
		this.log(message, Level.INFO);
	}

	/**
	 * print warn log
	 * 
	 * @param message
	 */
	public void warning(String message) {
		this.log(message, Level.WARNING);
	}

	/**
	 * print info log
	 * 
	 * @param message
	 */
	public synchronized void info(String message) {
		this.log(message, Level.INFO);
	}

	/**
	 * print log with target log level
	 * 
	 * @param message
	 * @param level
	 */
	public synchronized void log(String message, Level level) {
		String date = formatter.format(Calendar.getInstance().getTime());
		this.log(Level.INFO, "[" + date + "]: Peer [peer_ID " + peerId + "] " + message);
	}

	@Override
	public synchronized void log(Level level, String message) {
		super.log(level, message);
		super.log(level, "\n");
	}
}
