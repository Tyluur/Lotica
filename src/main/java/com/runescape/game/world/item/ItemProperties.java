package com.runescape.game.world.item;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.utility.Utils;
import com.runescape.utility.world.item.ItemWeights;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 18, 2015
 */
public class ItemProperties {

	/**
	 * The list of items by name that are untradeable
	 */
	private static List<String> untradeables;

	/**
	 * The list of items by name and id that can be exchanged
	 */
	private static List<Integer> exchangeables;

	/**
	 * Loads all foreign properties of items
	 */
	public static void loadProperties() {
		setUntradeables(Utils.getFileText("./data/resource/items/nontradeables.txt"));
		ItemWeights.init();
	}

	/**
	 * @param untradeables
	 * 		the untradeables to set
	 */
	public static void setUntradeables(List<String> untradeables) {
		ItemProperties.untradeables = untradeables;
	}

	/**
	 * Loads all items that are exchangeable
	 */
	public static void loadExchangeables() {
		exchangeables = new ArrayList<>();
		try {
			List<String> text = Files.readAllLines(new File("./data/resource/items/exchange/full_exchange_list.txt").toPath(), Charset.defaultCharset());
			for (String line : text) {
				if (line.startsWith("//")) { continue; }
				String[] split = line.split(": ");
				exchangeables.add(Integer.parseInt(split[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if an item is exchangeable, based on whether its in the {@link #exchangeables} list
	 *
	 * @param itemId
	 * 		The id of the item
	 */
	public static boolean isExchangeable(int itemId) {
		if (exchangeables == null) { loadExchangeables(); }
		return exchangeables.contains(itemId);
	}

	/**
	 * Checks if an item is untradeable by parsing through the list of {@link #untradeables} and checking for the item's
	 * name
	 *
	 * @param item
	 * 		The item
	 */
	public static boolean isUntradeable(Item item) {
		int itemId = item.getId();
		ItemDefinitions definitions = item.getDefinitions();
		for (String listName : untradeables) {
			if (Utils.isNumeric(listName)) {
				int id = Integer.parseInt(listName);
				if (itemId == id) {
					return true;
				}
			} else {
				if (definitions.getName().equalsIgnoreCase(listName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the list of exchangeables
	 */
	public static List<Integer> getExchangeables() {
		return exchangeables;
	}
}
