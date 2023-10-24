package main.Datahandler;

import java.io.*;

import main.constants.Constants;
import main.helper.CommonConfigHelper;

/**
 * Class to Manage the data segments
 */
public class ManageDataSegments {

    private RandomAccessFile outputStream;
    private FileInputStream inputStream;
    int segmentSize;
    private static ManageBitFields segmentFields;
    private static volatile ManageDataSegments object;
    int numOfSegments;

    /**
     * Return the singleton object of the ManageDataSegments class if present; otherwise, create one and return it.
     *
     * @param fileAlreadyDownloaded Indicates whether the file has already been downloaded
     * @param id Unique identifier for the file
     * @return The ManageDataSegments instance
     */
    public synchronized static ManageDataSegments returnSingletonInstance(boolean fileAlreadyDownloaded, String id) {
        if (object != null) {
            return object;
        } else {
            object = new ManageDataSegments();
            object = object.attachAllParameters(id, fileAlreadyDownloaded);
            return object;
        }
    }

    /**
     * Read and attach all parameters present in the configuration file.
     *
     * @param id                  Unique identifier for the file
     * @param fileAlreadyDownloaded Indicates whether the file has already been downloaded
     * @return The ManageDataSegments instance
     */
    public ManageDataSegments attachAllParameters(String id, boolean fileAlreadyDownloaded) {
        if (CommonConfigHelper.getConfig("PieceSize") == null ||
            CommonConfigHelper.getConfig("FileSize") == null ||
            CommonConfigHelper.getConfig("FileName") == null) {
            return null;
        }
        try {
            segmentSize = Integer.decode(CommonConfigHelper.getConfig("PieceSize"));
            numOfSegments = (int) Math.ceil(Integer.decode(CommonConfigHelper.getConfig("FileSize")) / ((double) segmentSize));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            segmentFields = new ManageBitFields(numOfSegments);
            if (!fileAlreadyDownloaded) {
                segmentFields.fillTheSegmentArrayWithNumber(0);
            } else {
                segmentFields.fillTheSegmentArrayWithNumber(1);
            }
            File dir = new File(id);
            boolean makeDir = !fileAlreadyDownloaded ? dir.mkdir() : false;
            outputStream = new RandomAccessFile(dir.getAbsolutePath() + "/" + CommonConfigHelper.getConfig("FileName"), "rw");
            outputStream.setLength(Integer.parseInt(CommonConfigHelper.getConfig(Constants.FILE_SIZE)));
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Close input and output streams.
     */
    synchronized public void closeIOStreams() {
        try {
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
        }
    }
}
