package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/11/2016
 */
public class ItemAmountCounter extends GameScript {

	/**
	 * The map with counting
	 */
	private static final Map<String, Long> AMOUNT_MAP = new HashMap<>();

	/**
	 * The keywords to search that will increment the amounts
	 */
	private static final String[] KEYWORDS = { "coins" };

	/**
	 * The total amount
	 */
	private static long totalAmount = 0;

	@Override
	protected String getFileLocation() {
		return "./info/out/item_amount_counter.txt";
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Cache.init();
		for (File file : getAccounts()) {
			try {
				Player player = getPlayer(file);
				if (player == null) {
					continue;
				}
				for (Item item : player.getInventory().getItems().toArray()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : KEYWORDS) {
						if (name.contains(contained.toLowerCase())) {
							insertData(player.getUsername(), item.getAmount());
						}
					}
				}
				for (Item item : player.getEquipment().getItems().toArray()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : KEYWORDS) {
						if (name.contains(contained.toLowerCase())) {
							insertData(player.getUsername(), item.getAmount());
						}
					}
				}
				for (Item item : player.getBank().getContainerCopy()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : KEYWORDS) {
						if (name.contains(contained.toLowerCase())) {
							insertData(player.getUsername(), item.getAmount());
						}
					}
				}
				if (player.getFamiliar() != null) {
					if (player.getFamiliar().getBob() != null) {
						for (Item item : player.getFamiliar().getBob().getBeastItems().toArray()) {
							if (item == null) {
								continue;
							}
							String name = item.getName().toLowerCase();
							for (String contained : KEYWORDS) {
								if (name.contains(contained.toLowerCase())) {
									insertData(player.getUsername(), item.getAmount());
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		ValueComparator bvc = new ValueComparator(AMOUNT_MAP);
		TreeMap<String, Long> sorted_map = new TreeMap<>(bvc);
		sorted_map.putAll(AMOUNT_MAP);

		ItemAmountCounter counter = new ItemAmountCounter();
		counter.write("The was a total of " + Utils.format(totalAmount) + " items matching keywords: " + Arrays.toString(KEYWORDS) + ".\n\n");

		for (Entry<String, Long> entry : sorted_map.entrySet()) {
			String message = entry.getKey() + "\t has " + Utils.format(entry.getValue()) + " total amounts that match keywords: " + Arrays.toString(KEYWORDS);
			counter.write(message);
		}
		System.out.println("DONE!");
	}

	private static void insertData(String username, int amount) {
		Long value = AMOUNT_MAP.get(username);
		if (value == null) {
			value = (long) amount;
		} else {
			value = value + amount;
		}
		AMOUNT_MAP.put(username, value);
		totalAmount += amount;
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
