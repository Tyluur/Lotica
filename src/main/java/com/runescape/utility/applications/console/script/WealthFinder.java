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
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/6/2015
 */
public class WealthFinder extends GameScript {

	private static final Map<String, Long> MAP = new HashMap<>();

	@Override
	protected String getFileLocation() {
		return "./info/out/networth.txt";
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player != null) {
					player.setUsername(acc.getName());
					long worth = 0;
					for (Item item : player.getInventory().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
					}
					for (Item item : player.getEquipment().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
					}
					for (Item item : player.getBank().generateContainer()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
					}
					worth += player.getFacade().getMoneyPouchCoins();
					if (player.getFamiliar() != null) {
						if (player.getFamiliar().getBob() != null) {
							for (Item item : player.getFamiliar().getBob().getBeastItems().toArray()) {
								if (item == null) {
									continue;
								}
								worth += getItemPrice(item);
							}
						}
					}
					if (worth > 0) {
						MAP.put(player.getUsername(), worth);
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
			String message = entry.getKey() + "\t has a networth of:\t" + Utils.format(entry.getValue());
			new WealthFinder().write(message);
			System.out.println(message);
		}
		System.out.println("DONE!");
	}

	/**
	 * Gets the real price of an item.
	 *
	 * @param item
	 * 		The item
	 */
	private static int getItemPrice(Item item) {
		return item.getId() == 995 ? item.getAmount() : item.getAmount() * getValue(item);
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
