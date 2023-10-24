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

    /**
     * Check if the logger has been initialized.
     *
     * @return true if the logger is initialized, false otherwise
     */
    public boolean isLoggerInitialized() {
        return this.logWriter != null;
    }

    /**
     * Create the log file and initialize the write handler object.
     */
    private void createLogFile() {
        String directory = "" + Constants.LOG_DIRECTORY_NAME;
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }

        this.fileName = String.format("%s/%s%s.log", directory, Constants.LOG_FILE_PREFIX, peerID);
        try {
            this.logWriter = new BufferedWriter(new FileWriter(this.fileName));
        } catch (IOException e) {
            System.out.printf("Exception occurred while creating log file. Message: %s", e.getMessage());
        }
    }

    /**
     * Log a message to the specified file along with the current date and time.
     *
     * @param message The log message to be written
     */
    public synchronized void logMessage(String message) {
        try {
            Date date = Calendar.getInstance().getTime();
            this.logWriter.write(String.format("[%s]: %s\n", date, message) + "\n");
            this.logWriter.flush();
        } catch (IOException e) {
            System.out.printf("Exception occurred while writing to log file: %s. Message: %s", this.fileName, e.getMessage());
        }
    }

    /**
     * Close the log writer object.
     */
    public void destroy() {
        try {
            if (this.logWriter != null) {
                this.logWriter.close();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while closing the logger object");
        }
    }
}
