package main.helper;

import java.util.*;
import java.io.*;

/**
 * This class is a helper class which contains info related to the available
 * pieces of a download file for a given peer
 * 
 * @author Adithya KNG
 */
public class BitFieldHelper implements Serializable {

    private final int[] segmentArray;

    /**
     * Constructor to initialize the bit field with the given number of pieces.
     *
     * @param numOfPieces The total number of segments or pieces
     */
    public BitFieldHelper(int numOfPieces) {
        segmentArray = new int[numOfPieces];
        Arrays.fill(segmentArray, 0); // Initialize all segments to not downloaded (0)
    }

    /**
     * Get the total number of segments in the bit field.
     *
     * @return The number of segments
     */
    public int getNumberOfSegments() {
        return segmentArray.length;
    }

    /**
     * Set the value (downloaded or not downloaded) at the given index in the bit
     * field.
     *
     * @param index The index of the segment
     * @param value True if downloaded, false if not downloaded
     */
    synchronized public void setValueAtIndex(int index, boolean value) {
        segmentArray[index] = value ? 1 : 0; // Set to 1 if downloaded, 0 if not downloaded
    }

    /**
     * Fill the entire bit field with the given number, indicating complete download
     * or not downloaded.
     *
     * @param number The value to fill the bit field with (1 for downloaded, 0 for
     *               not downloaded)
     */
    public void fillTheSegmentArrayWithNumber(int number) {
        Arrays.fill(segmentArray, number);
    }

    /**
     * Get the count of downloaded segments in the bit field.
     *
     * @return The count of downloaded segments
     */
    public int getCountOfDownloadedSegments() {
        if (segmentArray != null) {
            int segmentCount = 0;
            for (int segment : segmentArray) {
                if (segment == 1) {
                    segmentCount++;
                }
            }
            return segmentCount;
        }
        return 0;
    }

    /**
     * Check if all segments in the bit field are downloaded.
     *
     * @return True if all segments are downloaded, false otherwise
     */
    public boolean checkIfFileIsDownloaded() {
        if (segmentArray != null) {
            boolean ret = true;
            for (int segment : segmentArray) {
                if (segment == 0) {
                    ret = false;
                    break;
                }
            }
            return ret;
        }
        return false;
    }

    /**
     * Get the value (downloaded or not downloaded) at the given index in the bit
     * field.
     *
     * @param index The index of the segment
     * @return 1 if downloaded, 0 if not downloaded
     */
    public int getValueAtIndex(int index) {
        return segmentArray[index];
    }

    public void displayBitMap() {
        System.out.println("The bitMap is: ");
        for (int i = 0; i < segmentArray.length; i++) {
            System.out.print(" " + i + " : " + segmentArray[i]);
        }

        System.out.println();
    }
}