package main.Datahandler;

import java.util.*;
import java.io.*;


public class ManageBitFields implements Serializable {

	private final int[] segmentArray;

	/**
	 * At the begining no segments are present, so initalize with 0
	 */
	public ManageBitFields(int numOfPieces) {
		segmentArray = new int[numOfPieces];
		Arrays.fill(segmentArray, 0);
	}

	/**
	 * Get the total number of segments
	 */
	public int getNumberOfSegments() {
		return segmentArray.length;
	}

	/**
	 * Put the value at given index
	 */
	synchronized public void setValueAtIndex(int index, boolean value) {
		segmentArray[index] = value ?  1 : 0;
	}

	/**
	 * Fill the segment Array with number, indicating complete download/not downloaded
	 */
	public void fillTheSegmentArrayWithNumber(int number) {
		Arrays.fill(segmentArray, number);
	}

	/**
	 * Get the count of downloaded segments
	 */
	public int getCountOfDownloadedSegments() {
		if(segmentArray != null){
			int segmentCount = 0;
			for(int segment: segmentArray){
				if(segment == 1){
					segmentCount++;
					break;
				}
			}
			return segmentCount;
		}
		return 0;
	}

	/**
	 * Check if all segments of the file are downloaded
	 */
	public boolean checkIfFileIsDownloaded() {
		if(segmentArray != null){
			boolean ret = true;
			for(int segment: segmentArray){
				if(segment == 0){
					ret = false;
					break;
				}
			}
			return ret;
		}
		return false;
	}

	/**
	 * Retrun the value present at the given index
	 */
	public int getValueAtIndex(int index) {
		return segmentArray[index];
	}
}