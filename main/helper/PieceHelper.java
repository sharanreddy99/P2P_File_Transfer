package main.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import main.constants.Constants;
import main.messageTypes.Piece;

/**
 * This class is a helper class for dealing with piece related information for
 * the download files available within a given peer.
 * 
 * @author Sharan Sai Reddy Konda
 */
public class PieceHelper {

	int numOfPieces, pieceSize;

	private RandomAccessFile outStream;
	private FileInputStream inStream;

	private static BitFieldHelper bitFieldHelper;
	private static PieceHelper instance;

	/**
	 * Returns the singleton instance of the Piece Helper
	 * 
	 * @param isFileExists - a boolean indicating whether the file is already
	 *                     downloaded or not
	 * @param peerID       - a number indicating the unique identifier for the given
	 *                     peer
	 * @return - returns the singleton instance of the Piece Helper
	 */
	public synchronized static PieceHelper returnSingletonInstance(boolean isFileExists, String peerID) {
		if (instance != null) {
			return instance;
		}

		instance = new PieceHelper();
		if (instance.configPieceHelper(isFileExists, peerID) == false)
			instance = null;

		return instance;
	}

	/**
	 * configPieceHelper configures the piece helper for a given peer by setting up
	 * the output file directory
	 * 
	 * @param isFileExists - a boolean indicating whether the file is already
	 *                     downloaded or not
	 * @param peerID       - a number indicating the unique identifier for the given
	 *                     peer
	 * @return - boolean indicating whether the configuration was successful or not
	 */
	public boolean configPieceHelper(boolean isFileExists, String peerID) {

		// Compute the number of pieces based on piece and file size
		pieceSize = Integer.parseInt(CommonConfigHelper.getConfig(Constants.PIECE_SIZE_LABEL));
		float pieceSizeFloat = (float) pieceSize;
		numOfPieces = (int) Math
				.ceil(Float.parseFloat(CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL)) / pieceSizeFloat);

		try {
			// create a bit field helper and update the bit fields if the file has already
			// been downloaded.
			bitFieldHelper = new BitFieldHelper(numOfPieces);
			if (isFileExists) {
				bitFieldHelper.fillTheSegmentArrayWithNumber(1);
			}

			// create the peer director for storing the downloaded file if it doesnt exist
			// already
			File directory = new File(peerID);
			if (!isFileExists) {
				directory.mkdir();
			}

			// create the empty download file inside the newly created directory
			String outputFileName = String.format("%s/%s", directory.getAbsolutePath(),
					CommonConfigHelper.getConfig(Constants.FILE_NAME_LABEL));

			outStream = new RandomAccessFile(outputFileName, "rw");
			outStream.setLength(Integer.parseInt(CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL)));

			return true;
		} catch (Exception e) {
			System.out.printf("Exception occurred when configuring Piece Helper for the given peer. Message: %s",
					e.getMessage());
		}

		return false;
	}

	/**
	 * Returns the nth piece from the file if it exists. Otherwise returns null
	 * 
	 * @param index - index indicating the position of piece within the file
	 * @return Piece - returns the nth piece
	 */
	synchronized public Piece getNthPieceFromFile(int index) throws IOException {
		Piece newDataSegment = new Piece(pieceSize);

		// If the requested piece doesn't exist within the given peer, return null.
		if (bitFieldHelper.getValueAtIndex(index) == 0)
			return null;

		// Fetch the piece from the download file
		byte[] reqBytes = new byte[pieceSize];
		outStream.seek(index * pieceSize);
		int reqPieceSize = outStream.read(reqBytes);

		// If end of the file reached, piece doesnt exist
		if (reqPieceSize == -1) {
			return null;
		}

		// Store the fetched bytes inside the given piece.
		byte[] newReqBytes = new byte[reqPieceSize];
		System.arraycopy(reqBytes, 0, newReqBytes, 0, reqPieceSize);
		newDataSegment.setData(newReqBytes);

		return newDataSegment;
	}

	/**
	 * Write the given piece at offset index*pieceSize if the piece doesn't exist
	 * already for a given peer.
	 * 
	 * @param index - indicates the offset at which the piece has to be inserted
	 * @param null
	 */
	synchronized public void insertNthPiece(int index, Piece dataSegment) throws IOException {
		// If the piece doesn't exist with the peer, then write it to the file at the
		// specified offset.
		if (bitFieldHelper.getValueAtIndex(index) == 0) {
			outStream.seek(index * pieceSize);
			outStream.write(dataSegment.getData());

			// Update the bit field to 1 to indicate that the piece has been downloaded for
			// a given peer.
			bitFieldHelper.setValueAtIndex(index, true);
		}
	}

	/**
	 * checks if the file has been downloaded for a given peer.
	 * 
	 * @return boolean - indicates whether the file has been downloaded or not
	 */
	public synchronized boolean hasDownloadFileComplete() {
		return bitFieldHelper.checkIfFileIsDownloaded();
	}

	/**
	 * returns the BitField helper set for the given peer.
	 * 
	 * @return BitFieldHelper
	 */
	public BitFieldHelper getBitFieldHelper() {
		return bitFieldHelper;
	}

	/**
	 * close all the existing connections if they are created previously
	 * 
	 * @return null
	 */
	synchronized public void close() {
		try {
			if (inStream != null)
				inStream.close();
			if (outStream != null)
				outStream.close();

		} catch (Exception e) {
			System.out
					.printf("Exception occured while closing connections in Piece Manager. Message: " + e.getMessage());
		}
	}
}
