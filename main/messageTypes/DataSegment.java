package main.messageTypes;

import java.io.Serializable;

public class DataSegment implements Serializable {
    private byte[] data;
    private int size;

    /**
     * Constructor to initialize the DataSegment with a specific size.
     *
     * @param size The size of the data segment
     */
    public DataSegment(int size) {
        this.size = size;
    }

    /**
     * Set the data for the DataSegment.
     *
     * @param byteData The byte array data to set
     */
    public void setData(byte[] byteData) {
        this.data = byteData;
    }

    /**
     * Get the data stored in the DataSegment.
     *
     * @return The byte array data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the length of the data stored in the DataSegment.
     *
     * @return The length of the data
     */
    public int getDataLength() {
        return data != null ? data.length : 0;
    }

    /**
     * Get the size of the DataSegment.
     *
     * @return The size of the data segment
     */
    public int size() {
        return data != null ? getDataLength() : -1;
    }
}
