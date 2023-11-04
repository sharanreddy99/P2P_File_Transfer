package main.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import main.constants.Constants;

/**
 * This class extracts information related to the common configuration file.
 */
public class CommonConfigHelper {

    // Store common configuration data
    private static LinkedHashMap<String, String> configMap;

    static {
        configMap = new LinkedHashMap<String, String>();
        loadCommonConfigInfo();
    }

    /**
     * Load and extract common configuration information from the specified common config file.
     * 
     * @throws ExceptionInInitializerError if there is an error while loading the common config file
     */
    private static void loadCommonConfigInfo() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Constants.CONFIGURATION_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            String row;
            while ((row = bufferedReader.readLine()) != null) {
                row = row.trim();
                String[] tokens = row.split(" ");

                // Store key-value pairs in the configMap
                configMap.put(tokens[0].trim(), tokens[1].trim());
            }
			bufferedReader.close();

        } catch (IOException e) {
            String errorMessage = "Error while loading the common config file. Message: " + e.getMessage();
            System.out.printf("Exception occurred when extracting info from the common config file. Message: %s", e.getMessage());
            throw new ExceptionInInitializerError(errorMessage);
        }
    }

    /**
     * Get the configuration value based on the provided key.
     * 
     * @param key The key to retrieve the configuration value.
     * @return The configuration value associated with the key.
     */
    public static String getConfig(String key) {
        return configMap.get(key);
    }
}
