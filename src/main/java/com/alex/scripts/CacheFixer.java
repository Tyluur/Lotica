package com.alex.scripts;

import com.alex.loaders.items.ItemDefinitions;
import com.alex.store.Store;
import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.game.world.item.Item;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class CacheFixer {

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Store ours = new Store(GameConstants.CACHE_PATH);
		Cache.init();
		int[] ids = { 18839, 18344, 4278 };
		for (int i = 0; i < ids.length; i++) {
			ItemDefinitions definitions = ItemDefinitions.getItemDefinition(ours, ids[i]);
			definitions.write(ours, ((i >= ids.length - 1)));
			if (i % 1000 == 0) { System.out.println("Completed item " + i); }
		}
		System.out.println("done in " + (System.currentTimeMillis() - start) + " ms");
	}

	public static int[] getEquipInfo(int id) throws Exception {
		new Item(id);
		BufferedReader reader = new BufferedReader(new FileReader(new File("data/resource/items/equip.txt")));
		String line = "";
		while ((line = reader.readLine()) != null) {
			int lineId = Integer.parseInt(line.substring(0, line.indexOf(":")));
			if (lineId == id) {
				String info = line.substring(line.indexOf(":") + 1, line.length()).trim();
				String[] splitInfo = info.split(",");
				reader.close();
				return new int[] { Integer.parseInt(splitInfo[0].trim()), Integer.parseInt(splitInfo[1].trim()) };
			}
		}
		reader.close();
		return null;
	}
}
