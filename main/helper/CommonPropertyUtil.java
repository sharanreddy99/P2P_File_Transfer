package main.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import main.constants.Constants;

/**
 * common.cfg data
 */
public class CommonPropertyUtil {

	// config data
	private static final HashMap<String, String> propsMap = new HashMap<String, String>();

	static {
		init();
	}

	/**
	 * init config data in common.cfg
	 */
	private static void init() {
		try {
			BufferedReader configFileReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(Constants.CONFIGURATION_FILE)));
			String line = null;
			while ((line = configFileReader.readLine()) != null) {
				line = line.trim();

				String[] tokens = line.split(" ");
				propsMap.put(tokens[0].trim(), tokens[1].trim());
			}
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
			throw new ExceptionInInitializerError("Unable to load properties file");
		}
	}

	/**
	 * get config in common.cfg file
	 * 
	 * @param value
	 * @return
	 */
	public static String getProperty(String value) {
		return propsMap.get(value);
	}

}
