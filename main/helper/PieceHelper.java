package main.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import main.constants.Constants;
import main.messageTypes.Piece;

/**
 * Piece Manager
 */
public class PieceHelper {

	int numOfPieces; // num of piece
	int size; // piece size

	private RandomAccessFile outStream;
	private FileInputStream inStream;

	private static BitFieldHelper bitFieldManager;
	private static volatile PieceHelper instance;

	/**
	 * get instance
	 * 
	 * @param isFileExists
	 * @param peerID
	 * @return
	 */
	public synchronized static PieceHelper returnSingletonInstance(boolean isFileExists, String peerID) {
		if (instance == null) {
			instance = new PieceHelper();
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
	public boolean init(boolean isFileExists, String peerID) {
		// get config logMessage(: PieceSize
		if (CommonConfigHelper.getConfig(Constants.PIECE_SIZE_LABEL) != null)
			size = Integer.parseInt(CommonConfigHelper.getConfig(Constants.PIECE_SIZE_LABEL));
		else {
			// System.err.println("Piece Size not in Properties file. Invalid Properties
			// File!!!");
		}

		// get config logMessage(: FileSize
		if (CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL) != null) {
			numOfPieces = (int) Math
					.ceil(Integer.parseInt(CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL)) / (size * 1.0));
		}

		try {
			bitFieldManager = new BitFieldHelper(numOfPieces);
			if (isFileExists) {
				bitFieldManager.fillTheSegmentArrayWithNumber(1);
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
			outStream.setLength(Integer.parseInt(CommonConfigHelper.getConfig(Constants.FILE_SIZE_LABEL)));
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

	/**
	 * Gets the piece of file.
	 * 
	 * @param index
	 * @return
	 */
	synchronized public Piece get(int index) {
		Piece newDataSegment = new Piece(size);
		if (bitFieldManager.getValueAtIndex(index) == 1) {
			byte[] readBytes = new byte[size];
			int newSize = 0;
			// have to read this piece from my own output file.
			try {
				outStream.seek(index * size);
				newSize = outStream.read(readBytes);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if (newSize != size) {
				byte[] newReadBytes = new byte[newSize];
				if (newSize >= 0) {
					System.arraycopy(readBytes, 0, newReadBytes, 0, newSize);
				}
				newDataSegment.setData(newReadBytes);
			} else {
				newDataSegment.setData(readBytes);
			}
			return newDataSegment;
		} else {
			return null;
		}
	}

	/**
	 * write piece
	 * 
	 * @param index
	 * @param piece
	 */
	synchronized public void write(int index, Piece dataSegment) {
		if (bitFieldManager.getValueAtIndex(index) == 0) {
			try {
				// have to write this piece in Piece object array
				outStream.seek(index * size);
				outStream.write(dataSegment.getData());
				bitFieldManager.setValueAtIndex(index, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * the missing piece number.
	 *
	 * @return
	 */
	synchronized public int[] getMissingPieceNumberArray() {
		int count = 0, missSize = 0;
		// parse missing indexe count
		while (true) {
			if (count >= bitFieldManager.getNumberOfSegments())
				break;
			if (bitFieldManager.getValueAtIndex(count) == 0) {
				missSize++;
			}
			count++;
		}

		// creating an array of count size
		int[] missData = new int[missSize];
		count = 0;
		missSize = 0;
		while (true) {
			if (count >= bitFieldManager.getNumberOfSegments())
				break;

			if (bitFieldManager.getValueAtIndex(count) == 0) {
				missData[missSize++] = count;
			}
			count++;
		}
		bitFieldManager.displayBitMap();

		return missData;
	}

	/**
	 * check file download completed
	 * 
	 * @return
	 */
	public synchronized boolean hasDownloadFileComplete() {
		return bitFieldManager.checkIfFileIsDownloaded();
	}

	/**
	 * getBitField
	 * 
	 * @return
	 */
	public BitFieldHelper getBitField() {
		return bitFieldManager;
	}
}
