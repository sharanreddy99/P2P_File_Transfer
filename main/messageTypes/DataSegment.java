package main.messageTypes;

import java.io.Serializable;

/**
 * piece
 */
public class DataSegment implements Serializable {
	private byte[] byteData;
	int size;

	public DataSegment(int size) {
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
