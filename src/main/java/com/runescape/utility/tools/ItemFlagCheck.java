package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemProperties;
import com.runescape.utility.Utils;

import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/29/2015
 */
public class ItemFlagCheck {

	private static void addToFile(ItemDefinitions definitions) {
		Utils.writeTextToFile("untradeables.txt", definitions.getId() + ":\t" + definitions.getName() + "\n", true);
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		ItemProperties.loadProperties();
		Utils.clearFile("untradeables.txt");
		int count = 0;
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			ItemDefinitions definitions = ItemDefinitions.forId(itemId);
			if (definitions == null) {
				System.out.println("No definitions for " + itemId);
				continue;
			}
			if (ItemProperties.isUntradeable(new Item(itemId))) {
				addToFile(definitions);
				count++;
			}
		}
		System.err.println(count + " untradeables [" + count + "/" + (Utils.getItemDefinitionsSize() - 1) + "]");
	}

}
