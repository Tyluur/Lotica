package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.utility.Utils;

import java.io.File;
import java.io.IOException;

public class ItemCheck {

	public static final void main(String[] args) throws IOException {
		Cache.init();
		int total = 0;
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			if (ItemDefinitions.forId(itemId).isWearItem(true) && !ItemDefinitions.forId(itemId).isNoted()) {
				File file = new File("bonuses/" + itemId + ".txt");
				if (!file.exists()) {
					System.out.println(file.getName());
					total++;
				}
			}
		}
		System.out.println("Total " + total);
	}
}
