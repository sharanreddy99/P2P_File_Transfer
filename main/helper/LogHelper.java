package main.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;

import main.constants.Constants;

/**
 * This class helps in logging each action according to the specifications.
 */
public class LogHelper {
	private String peerID;
	private String fileName;
	private BufferedWriter logWriter;

	public LogHelper(String peerID) {
		this.peerID = peerID;
		this.createLogFile();
	}

	public boolean isLoggerInitialized() {
		return this.logWriter != null;
	}

	/**
	 * Create the log file path and initializes the write handler object.
	 * 
	 * @return null
	 */
	private void createLogFile() {
		String directory = "" + Constants.LOG_FILE_DIRECTORY_NAME;
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}

		this.fileName = String.format("%s/%s%s.log", directory, Constants.LOG_FILE_NAME_PREFIX, peerID);
		try {
			this.logWriter = new BufferedWriter(new FileWriter(this.fileName));
		} catch (IOException e) {
			System.out.printf("Exception occured while creating log file. Message: %s", e.getMessage());
		}
	}

	/**
	 * this function logs the appropriate message to the specified file
	 * 
	 * @param message - log message
	 * @return null
	 */
	public synchronized void logMessage(String message) {
		try {
			Date date = Calendar.getInstance().getTime();
			this.logWriter.write(String.format("[%s]: %s\n", date, message) + "\n");
			this.logWriter.flush();
		} catch (IOException e) {
			System.out.printf("Exception occured while writing to log file: %s. Message: %s", this.fileName,
					e.getMessage());
		}
	}

	/**
	 * 
	 * This function closes the log writer object.
	 * 
	 * @return null
	 */
	public void destroy() {
		try {
			if (this.logWriter != null) {
				this.logWriter.close();
			}
		} catch (Exception e) {
			System.out.println("Exception occured while closing the logger object");
		}
	}
}
