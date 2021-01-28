package com.alex.scripts;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.utility.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/15/2016
 */
public class ItemDefinitionDumper {

	public static void main(String[] args) {
		try {
			Cache.init();
			BufferedWriter writer = new BufferedWriter(new FileWriter("items.txt"));
			int size = Utils.getItemDefinitionsSize();
			System.out.println("Item definitions size=" + size);
			for (int i = 0; i < size; i++) {
				ItemDefinitions def = ItemDefinitions.forId(i);

				String message = i + ":\t" + def.getName() + "\n";

				System.out.print(message);
				writer.write(message);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
