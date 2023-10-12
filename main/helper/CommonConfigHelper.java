package main.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import main.constants.Constants;
import main.messageTypes.PeerInfo;

/**
 * This class extracts information related to the common configuration file
 */
public class CommonConfigHelper {

	// config data
	private static LinkedHashMap<String, String> configMap;

	static {
		configMap = new LinkedHashMap<String, String>();
		configCommonInfo();
	}

	/**
	 * This function extracts config info from the mentioned common config file
	 * 
	 * @return boolean indicating whether the extraction of common config info was
	 *         successful or not
	 */
	private static void configCommonInfo() {
		try {
			FileInputStream fir = new FileInputStream(Constants.CONFIGURATION_FILE);
			BufferedReader br = new BufferedReader(new InputStreamReader(fir));

			String row;
			for (; (row = br.readLine()) != null;) {
				row = row.trim();
				String[] tokens = row.split(" ");

				configMap.put(tokens[0].trim(), tokens[1].trim());
			}

		} catch (IOException e) {
			System.out.printf("Exception occured when extracting info from the common config file. Message: %s",
					e.getMessage());
			throw new ExceptionInInitializerError(
					"Error while loading the common config file. Message: " + e.getMessage());
		}
	}

	/**
	 * returns the single config info based on key
	 * 
	 * @return a single config value
	 */
	public static String getConfig(String key) {
		return configMap.get(key);
	}

}
