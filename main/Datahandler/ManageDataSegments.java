package main.Datahandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import main.constants.Constants;
import main.helper.CommonConfigHelper;

/**
 * Piece Manager
 */
public class ManageDataSegments {

	int numOfPieces; // num of piece
	int size; // piece size

	private RandomAccessFile outStream;
	private FileInputStream inStream;

	private static ManageBitFields bitField;
	private static volatile ManageDataSegments instance;

	/**
	 * get instance
	 * 
	 * @param isFileExists
	 * @param peerID
	 * @return
	 */
	public synchronized static ManageDataSegments returnSingletonInstance(boolean isFileExists, String peerID) {
		if (instance == null) {
			instance = new ManageDataSegments();
			if (!instance.init(isFileExists, peerID)) {
				instance = null;
			}
		}
		return instance;
	}

	/**
	 * init
	 * 
	 * @param isFileExists
	 * @param peerID
	 * @return
	 */
	//Required
	public boolean init(boolean isFileExists, String peerID) {
		// get config logMessage(: PieceSize
		if (CommonConfigHelper.getConfig("PieceSize") != null)
			size = Integer.parseInt(CommonConfigHelper.getConfig("PieceSize"));
		else {
			// System.err.println("Piece Size not in Properties file. Invalid Properties
			// File!!!");
		}

		// get config logMessage(: FileSize
		if (CommonConfigHelper.getConfig("FileSize") != null) {
			numOfPieces = (int) Math.ceil(Integer.parseInt(CommonConfigHelper.getConfig("FileSize")) / (size * 1.0));
		}

		try {
			bitField = new ManageBitFields(numOfPieces);
			if (isFileExists) {
				bitField.setBitFieldOnForAllIndexes();
			}
			String outputFileName = CommonConfigHelper.getConfig("FileName");

			// String directoryName = "peer_" + peerID;
			String directoryName = peerID;
			File directory = new File(directoryName);

			if (!isFileExists) {
				directory.mkdir();
			}

			outputFileName = directory.getAbsolutePath() + "/" + outputFileName;
			outStream = new RandomAccessFile(outputFileName, "rw");
			outStream.setLength(Integer.parseInt(CommonConfigHelper.getConfig(Constants.FILE_SIZE)));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * close
	 */
	synchronized public void close() {
		try {
			if (outStream != null) {
				outStream.close();
			}
		} catch (Exception ignore) {
		}

		try {
			if (inStream != null) {
				inStream.close();
			}
		} catch (Exception ignore) {
		}

	}
}
