package main.handlers.filehandler;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Bit handler
 */
public class BitFieldHandler implements Serializable {

	private final boolean[] bitfieldVector; // bitfield vector
	private final int size;

	/**
	 * The construct
	 * 
	 * @param numOfPieces the num of pieces
	 */
	public BitFieldHandler(int numOfPieces) {
		bitfieldVector = new boolean[numOfPieces];
		size = numOfPieces;
		for (int i = 0; i < size; i++) {
			bitfieldVector[i] = false;
		}
	}

	/**
	 * getSize
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * get bitfield value
	 * 
	 * @param index
	 * @return
	 */
	public boolean getBitField(int index) {
		return bitfieldVector[index];
	}

	/**
	 * set
	 * 
	 * @param index
	 * @param value
	 */
	synchronized public void setBitField(int index, boolean value) {
		bitfieldVector[index] = value;
	}

	/**
	 * print bitvector
	 */
	public void printVector() {
		System.out.println(" printing bitvector");
		int i = 0;
		while (i < size) {
			System.out.print(" " + i + " : " + bitfieldVector[i++]);
		}
		System.out.println();
	}

	/**
	 * fill
	 */
	public void setBitFieldOnForAllIndexes() {
		Arrays.fill(bitfieldVector, true);
	}

	/**
	 *
	 * @return
	 */
	public int getNoOfPieces() {
		int counter = 0;
		boolean[] vector = this.bitfieldVector;
		for (int i = 0, vectorLength = vector.length; i < vectorLength; i++) {
			if (vector[i]) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * whether the file download completed
	 * 
	 * @return
	 */
	public boolean isFileDownloadComplete() {
		if (bitfieldVector == null || bitfieldVector.length == 0) {
			return false;
		}
		for (int i = 0; i < getSize(); i++) {
			if (!bitfieldVector[i]) {
				return false;
			}
		}
		return true;
	}
}