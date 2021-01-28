package com.runescape.utility.world.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 9, 2015
 */
public class PlayerSaving {

	/**
	 * Saves the player to the json file
	 *
	 * @param player
	 * 		The player
	 */
	public static void savePlayer(Player player) {
		try (Writer writer = new FileWriter(FILES_LOCATION + player.getUsername() + SUFFIX)) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC);
			Gson gson = builder.create();
			gson.toJson(player, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creating a player object from a saved player file
	 *
	 * @param name
	 * 		The name of the file
	 */
	public static Player fromFile(String name) {
		try {
			File file = new File(getFileLocation(name));
			// The file is too big; its nulled. Instead of dedicating resources we will return a null player
			// which will stop the login
			if (file.length() > 1_000_000) {
				System.err.println("Error reading file: " + file.getAbsolutePath());
				return null;
			}
			return GSON.fromJson(Utils.getText(getFileLocation(name)), Player.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The location of the file for the player
	 *
	 * @param name
	 * 		The name of the player
	 */
	private static String getFileLocation(String name) {
		return FILES_LOCATION + name + SUFFIX;
	}

	/**
	 * The suffix of the file
	 */
	public static final String SUFFIX = ".p";

	/**
	 * The location in which player files are saved
	 */
	public static final String FILES_LOCATION = GameConstants.FILES_PATH + "players/accounts/";

	/**
	 * The gson instance for reading from files
	 */
	private static final Gson GSON = new Gson();

	/**
	 * @param name
	 * 		The name of the player to check for
	 */
	public static boolean playerExists(String name) {
		return new File(getFileLocation(name)).exists();
	}

}