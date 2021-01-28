package com.runescape.utility.world.player;

import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/29/2015
 */
public class StarterList {

	/**
	 * The max amount of starters a single address can have
	 */
	public static final int MAX_STARTERS_PER_ADDRESS = 2;

	/**
	 * The map of starters that were received, with the key being the mac address and the value being the amount of
	 * starters received.
	 */
	private static final Map<String, Integer> starterMap = new HashMap<>();

	/**
	 * The directory of the file that will have starter information
	 */
	private static final String FILE_DIRECTORY = GameConstants.FILES_PATH + "starters.txt";

	/**
	 * The object to synchronize functions with
	 */
	private static final Object LOCK = new Object();

	/**
	 * Loads all starters from the file in {@link #FILE_DIRECTORY} into the {@link #starterMap} sequentially
	 */
	public static void loadStarters() {
		synchronized (LOCK) {
			starterMap.clear();
			for (String line : Utils.getFileText(FILE_DIRECTORY)) {
				String[] split = line.split("\t->\t");
				String mac = split[0];
				Integer starters = Integer.parseInt(split[1]);
				starterMap.put(mac, starters);
			}
		}
	}

	/**
	 * This method dumps all the starters from the {@link #starterMap} into the {@link #FILE_DIRECTORY} file.
	 */
	private static void dumpStarters() {
		synchronized (LOCK) {
			Utils.clearFile(FILE_DIRECTORY);
			for (Entry<String, Integer> entry : starterMap.entrySet()) {
				String mac = entry.getKey();
				Integer starters = entry.getValue();
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_DIRECTORY, true))) {
					writer.write(mac + "\t->\t" + starters);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Dumped " + starterMap.size() + " starters to file.");
		}
	}

	/**
	 * Finds out how many starters were received from an address
	 *
	 * @param macAddress
	 * 		The address to check for
	 */
	public static int getStartersReceived(String macAddress) {
		synchronized (LOCK) {
			Integer starters = starterMap.get(macAddress);
			if (starters == null) {
				return 0;
			}
			return starters;
		}
	}

	/**
	 * Finds out if the mac address can receive any more starters
	 *
	 * @param macAddress
	 * 		The mac address
	 */
	public static boolean canReceiveStarter(String macAddress) {
		synchronized (LOCK) {
			int startersReceived = getStartersReceived(macAddress);
			return startersReceived < MAX_STARTERS_PER_ADDRESS;
		}
	}

	/**
	 * Inserts the mapAddress to the {@link #starterMap} and reloads the map
	 *
	 * @param macAddress
	 * 		The address to insert.
	 */
	public static void insertStarter(String macAddress) {
		synchronized (LOCK) {
			starterMap.put(macAddress, getStartersReceived(macAddress) + 1);
			dumpStarters();
			loadStarters();
		}
	}

}
