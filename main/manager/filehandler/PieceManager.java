package main.manager.filehandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import main.constants.Constants;
import main.helper.CommonPropertyUtil;
import main.message.meta.Piece;

/**
 * Piece Manager
 */
public class PieceManager {

	int numOfPieces; // num of piece
	int size; // piece size

	private RandomAccessFile outStream;
	private FileInputStream inStream;

	private static BitFieldHandler bitField;
	private static volatile PieceManager instance;

	/**
	 * get instance
	 * 
	 * @param isFileExists
	 * @param peerID
	 * @return
	 */
	public synchronized static PieceManager getInstance(boolean isFileExists, String peerID) {
		if (instance == null) {
			instance = new PieceManager();
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
		// get config info: PieceSize
		if (CommonPropertyUtil.getProperty("PieceSize") != null)
			size = Integer.parseInt(CommonPropertyUtil.getProperty("PieceSize"));
		else {
			// System.err.println("Piece Size not in Properties file. Invalid Properties
			// File!!!");
		}

		// get config info: FileSize
		if (CommonPropertyUtil.getProperty("FileSize") != null) {
			numOfPieces = (int) Math.ceil(Integer.parseInt(CommonPropertyUtil.getProperty("FileSize")) / (size * 1.0));
		}

		try {
			bitField = new BitFieldHandler(numOfPieces);
			if (isFileExists) {
				bitField.setBitFieldOnForAllIndexes();
			}
			String outputFileName = CommonPropertyUtil.getProperty("FileName");

			// String directoryName = "peer_" + peerID;
			String directoryName = peerID;
			File directory = new File(directoryName);

			if (!isFileExists) {
				directory.mkdir();
			}

			outputFileName = directory.getAbsolutePath() + "/" + outputFileName;
			outStream = new RandomAccessFile(outputFileName, "rw");
			outStream.setLength(Integer.parseInt(CommonPropertyUtil.getProperty(Constants.FILE_SIZE)));
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
		Piece newPiece = new Piece(size);
		if (bitField.getBitField(index)) {
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
				newPiece.setByteData(newReadBytes);
			} else {
				newPiece.setByteData(readBytes);
			}
			return newPiece;
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
	synchronized public void write(int index, Piece piece) {
		if (!bitField.getBitField(index)) {
			try {
				// have to write this piece in Piece object array
				outStream.seek(index * size);
				outStream.write(piece.getByteData());
				bitField.setBitField(index, true);
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
			if (count >= bitField.getSize())
				break;
			if (!bitField.getBitField(count)) {
				missSize++;
			}
			count++;
		}

		// creating an array of count size
		int[] missData = new int[missSize];
		count = 0;
		missSize = 0;
		while (true) {
			if (count >= bitField.getSize())
				break;

			if (!bitField.getBitField(count)) {
				missData[missSize++] = count;
			}
			count++;
		}
		bitField.printVector();

		return missData;
	}

	/**
	 * check file download completed
	 * 
	 * @return
	 */
	public synchronized boolean hasDownloadFileComplete() {
		return bitField.isFileDownloadComplete();
	}

	/**
	 * getBitField
	 * 
	 * @return
	 */
	public BitFieldHandler getBitField() {
		return bitField;
	}
}
