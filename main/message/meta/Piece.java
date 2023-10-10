package main.message.meta;

import java.io.Serializable;

/**
 * piece
 */
public class Piece implements Serializable {
	private byte[] byteData;
	int size;

	public Piece(int size) {
		this.size = size;
	}

	public byte[] getByteData() {
		return byteData;
	}

	public void setByteData(byte[] byteData) {
		this.byteData = byteData;
	}

	public int getSize() {
		if (byteData == null) {
			return -1;
		} else {
			return byteData.length;
		}
	}
}
