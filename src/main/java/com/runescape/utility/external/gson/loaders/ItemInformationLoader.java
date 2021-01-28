package com.runescape.utility.external.gson.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.resource.ItemInformation;
import com.runescape.workers.game.core.CoresManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class ItemInformationLoader {

	public static void main(String[] args) {
		/*List<String> oldText = Utils.getFileText("./data/resource/items/exchange/rs_price_list.txt");
		System.out.println(oldText);
		List<String> newText = new ArrayList<>();
		for (String text : oldText) {
			text = text.replaceAll("\\<.*?>","");
			newText.add(text);
		}
		newText.forEach(text -> {
			Utils.writeTextToFile("./data/resource/items/exchange/rs_price_list1.txt", text + "\n", true);
		});*/
		try {
			Cache.init();
			CoresManager.init();
			GsonStartup.loadAll();
			List<String> oldText = Utils.getFileText("./data/resource/items/exchange/rs_price_list1.txt");
			Map<Integer, Integer> prices = new HashMap<>();
			for (String line : oldText) {
				String[] split = line.split(" - ");
				try {
					int itemId = Integer.parseInt(split[0]);
					int price = Integer.parseInt(split[1]);
					prices.put(itemId, price);
				} catch (Exception e) { }
			}
			for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
				ItemInformation information = getInformation(i);
				if (information == null) {
					continue;
				}
				Integer price = prices.get(i);
				if (price != null) {
					information.setRealPrice(price);
					saveData(i, information);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds the bonuses of an item
	 *
	 * @param itemId
	 * 		The item
	 */
	public static int[] getBonuses(int itemId) {
		ItemInformation info = getInformation(itemId);
		return info == null ? null : info.getBonuses();
	}

	/**
	 * Gets the examined information of an item
	 *
	 * @param itemId
	 * 		The id of the item
	 */
	public static String getExamine(int itemId) {
		ItemInformation info = getInformation(itemId);
		String description = info == null ? "It's an item." : info.getExamine();
		return description + (GameConstants.DEBUG ? "[itemId=" + itemId + "]" : "");
	}

	/**
	 * Gets the {@code ItemInformation} of an item
	 *
	 * @param itemId
	 * 		The id of the item
	 */
	public static ItemInformation getInformation(int itemId) {
		synchronized (LOCK) {
			if (!hasDataForItem(itemId)) {
				return null;
			}
			ItemInformation info = CACHED_DATA.get(itemId);
			if (info == null) {
				CACHED_DATA.put(itemId, info = loadFromFile(itemId));
			}
			return info;
		}
	}

	/**
	 * This method loads and constructs a new {@code ItemInformation} {@code Object} from the {@link
	 * #getFileLocation(int)} based on the itemId
	 *
	 * @param itemId
	 * 		The id of the item
	 */
	private static ItemInformation loadFromFile(int itemId) {
		String text = Utils.getText(getFileLocation(itemId));
		return GSON.fromJson(text, ItemInformation.class);
	}

	/**
	 * Saves the data to a file
	 *
	 * @param itemId
	 * 		The item's id
	 * @param information
	 * 		The data
	 */
	public static void saveData(int itemId, ItemInformation information) {
		try (Writer writer = new FileWriter(getFileLocation(itemId))) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();
			gson.toJson(information, writer);
			System.out.println("Stored item information for " + itemId + " (" + ItemDefinitions.getItemDefinitions(itemId).getName() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param itemId
	 * 		The id of the item
	 */
	private static String getFileLocation(int itemId) {
		return DATA_LOCATION + itemId + ".json";
	}

	/**
	 * If we have information for the item
	 *
	 * @param itemId
	 * 		The item id we're checking for
	 */
	private static boolean hasDataForItem(int itemId) {
		return new File(getFileLocation(itemId)).exists();
	}

	/**
	 * The gson instance
	 */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The location that data will be stored
	 */
	private static final String DATA_LOCATION = "./data/resource/items/data/";

	/**
	 * The map of cached data
	 */
	public static final Map<Integer, ItemInformation> CACHED_DATA = new HashMap<>();

	/**
	 * The lock to synchronize through
	 */
	private static final Object LOCK = new Object();
}
