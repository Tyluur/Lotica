package com.runescape.utility.applications.console.listener.responses;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.listener.ConsoleResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public class Itemn implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "itemn";
	}

	@Override
	public void onCall(String text) {
		List<ItemDefinitions> results = new ArrayList<>();
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			ItemDefinitions def = ItemDefinitions.forId(itemId);
			if (def == null || def.isNoted()) {
				continue;
			}
			String name = def.getName();
			if (name.toLowerCase().contains(text.toLowerCase())) {
				results.add(def);
			}
		}
		if (results.size() > 0) {
			System.out.println(results.size() + " results found for keyword '" + text + "'");
			results.forEach(def -> System.out.println("\t\t" + def.getName() + ": " + def.getId()));
			System.out.println("Finished search...");
		} else { System.out.println("No results fount for keyword '" + text + "'"); }
	}
}
