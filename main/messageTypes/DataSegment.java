package main.messageTypes;
import java.io.Serializable;
public class DataSegment implements Serializable {
	private byte[] data;
	int size;

	public DataSegment(int size) {
		this.size = size;
	}

	public void setData(byte[] byteData) {
		this.data = byteData;
	}

	public byte[] getData() {
		return data;
	}

	public int getDataLength(){
		return data.length;
	}

	public int size() {
		return data != null ? getDataLength() : -1;
	}
}
