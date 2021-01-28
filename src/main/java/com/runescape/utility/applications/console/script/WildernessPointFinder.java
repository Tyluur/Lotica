package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/21/2016
 */
public class WildernessPointFinder extends GameScript {

	private static final Map<String, Long> MAP = new HashMap<>();

	@Override
	protected String getFileLocation() {
		return "./info/out/wilderness_points.txt";
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player != null) {
					int worth = player.getFacade().getWildernessPoints();
					if (worth > 0) {
						MAP.put(player.getUsername(), (long) worth);
					}
				}
			} catch (Exception e) {
				System.err.println("Error on: " + acc.getAbsolutePath());
				e.printStackTrace();
			}
		}
		ValueComparator bvc = new ValueComparator(MAP);
		TreeMap<String, Long> sorted_map = new TreeMap<>(bvc);
		sorted_map.putAll(MAP);

		for (Entry<String, Long> entry : sorted_map.entrySet()) {
			String message = entry.getKey() + "\t has\t\t\t" + Utils.format(entry.getValue()) + " wilderness points.";
			new WildernessPointFinder().write(message);
			System.out.println(message);
		}
		System.out.println("DONE!");
	}

	/**
	 * Gets the value of an item from its definitions
	 *
	 * @param item
	 * 		The item
	 */
	private static int getValue(Item item) {
		return item.getDefinitions().getExchangePrice();
	}

	private static class ValueComparator implements Comparator<String> {

		private final Map<String, Long> base;

		public int compare(String a, String b) {
			return base.get(a).compareTo(base.get(b)) * (-1);
		}

		public ValueComparator(Map<String, Long> base) {
			this.base = base;
		}

	}


}
