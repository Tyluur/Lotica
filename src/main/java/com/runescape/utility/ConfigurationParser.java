package com.runescape.utility;

import com.runescape.game.GameConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/26/2015
 */
public class ConfigurationParser {

	/** The key for double experience */
	public static final String DXP_WEEKEND_START_KEY = "double_exp_start";

	/** The key for when double experience is finished */
	public static final String DXP_WEEKEND_END_KEY = "double_exp_end";

	/** The key for double votes */
	public static final String DVP_WEEKEND_KEY = "double_votes_enabled";

	/**
	 * The instance of the PROPERTIES
	 */
	private static final Properties PROPERTIES = new Properties();

	/**
	 * The location of the configuration files
	 */
	private static final String PROPERTIES_FILE_LOCATION = GameConstants.FILES_PATH + "configuration.txt";

	/**
	 * Loads configuration from the file and populates the {@link #PROPERTIES} with the data
	 */
	public static void loadConfiguration() throws IOException {
		// making the file if we don't have it
		File file = new File(PROPERTIES_FILE_LOCATION);
		if (!file.exists()) {
			file.createNewFile();
		}

		// loading properties from the file
		PROPERTIES.load(new FileReader(PROPERTIES_FILE_LOCATION));

		// loads the default properties
		loadDefaults();

		// setting the variables
		GameConstants.DOUBLE_EXPERIENCE_TIMES[0] = getLong(DXP_WEEKEND_START_KEY);
		GameConstants.DOUBLE_EXPERIENCE_TIMES[1] = getLong(DXP_WEEKEND_END_KEY);
		GameConstants.DOUBLE_VOTES_ENABLED = getBoolean(DVP_WEEKEND_KEY);
	}

	/**
	 * Loads the default properties
	 */
	private static void loadDefaults() throws IOException {
		if (!PROPERTIES.containsKey(DXP_WEEKEND_START_KEY)) {
			putProperty(DXP_WEEKEND_START_KEY, 0);
		}
		if (!PROPERTIES.containsKey(DXP_WEEKEND_END_KEY)) {
			putProperty(DXP_WEEKEND_END_KEY, 0);
		}
		if (!PROPERTIES.containsKey(DVP_WEEKEND_KEY)) {
			putProperty(DVP_WEEKEND_KEY, "false");
		}
	}

	/**
	 * Puts the property into the map
	 *
	 * @param key
	 * 		The key
	 * @param value
	 * 		The value
	 */
	public static void putProperty(String key, Object value) throws IOException {
		PROPERTIES.setProperty(key, "" + value);
		PROPERTIES.save(new FileOutputStream(new File(PROPERTIES_FILE_LOCATION)), "");
		loadConfiguration();
	}

	/**
	 * Gets a long
	 *
	 * @param key
	 * 		The key
	 */
	public static Long getLong(String key) {
		return Long.parseLong(PROPERTIES.getProperty(key));
	}

	/**
	 * Gets a boolean value from the configuration file
	 *
	 * @param string
	 * 		The key to search for
	 */
	public static boolean getBoolean(String string) {
		return Boolean.parseBoolean(PROPERTIES.getProperty(string));
	}

}
