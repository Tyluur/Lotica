package com.runescape.cache.loaders;

import com.runescape.cache.Cache;
import com.runescape.utility.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public final class ItemsEquipIds {

	private static final HashMap<Integer, Integer> itemsDefinitions = new HashMap<>();

	private ItemsEquipIds() {

	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		init();
		String file = "equipids.txt";
		Utils.clearFile(file);
		itemsDefinitions.entrySet().forEach(entry -> {
			Integer id = entry.getKey();
			Integer val = entry.getValue();

			Utils.writeTextToFile(file, id + "=" + val + "\n", true);
		});
	}

	public static void init() {
		List<String> lines = Utils.getFileText("./data/resource/items/equipids.txt");
		lines.forEach(line -> {
			String[] split = line.split("=");
			itemsDefinitions.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		});
	}

	public static int getEquipId(int itemId) {
		Integer equipId = itemsDefinitions.get(itemId);
		if (equipId == null) {
			return -1;
		}
		return equipId;
	}
}
