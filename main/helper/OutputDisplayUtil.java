package main.helper;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * OutputDisplayUtil
 */
public class OutputDisplayUtil implements Runnable {
	BufferedReader reader;
	String peerId;

	/**
	 * construct
	 * 
	 * @param peerId
	 * @param reader
	 */
	public OutputDisplayUtil(String peerId, BufferedReader reader) {
		this.reader = reader;
		this.peerId = peerId;
	}

	/**
	 * run
	 */
	public void run() {
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("[" + peerId + "]: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
