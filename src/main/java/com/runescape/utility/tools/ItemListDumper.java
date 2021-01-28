package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.utility.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ItemListDumper {

	public static void main(String[] args) {
		try {
			generateDump();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method generates a dump of all the items in the item definitions and stores them into a PTF file
	 */
	public static void generateDump() throws IOException {
		Cache.init();
		File file = new File("info/itemlist.txt");
		if (file.exists()) { file.delete(); } else { file.createNewFile(); }
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.append("//Version = 718\n");
		writer.flush();
		for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
			ItemDefinitions def = ItemDefinitions.forId(id);
			writer.append(def.getName() + ":\t" + id);
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}
}
