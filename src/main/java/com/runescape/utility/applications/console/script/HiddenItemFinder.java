package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/5/2016
 */
public class HiddenItemFinder extends GameScript {

	/**
	 * The flag that means a player is wealthy
	 */
	public static final int WEALTH_FLAG = 50_000_000;

	/**
	 * The flag that means that the item has a large value
	 */
	private static final int VALUE_FLAG = 1_000_000;

	/**
	 * The map of all values
	 */
	private static final Map<String, Map<Long, List<Item>>> VALUE_MAP = new HashMap<>();

	@Override
	protected String getFileLocation() {
		return "./info/out/hidden_items.txt";
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player != null) {
					List<Item> items = new ArrayList<>();

					player.setUsername(acc.getName());
					long worth = 0;
					for (Item item : player.getInventory().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
						if (getItemPrice(item) >= VALUE_FLAG) {
							items.add(item);
						}
					}
					for (Item item : player.getEquipment().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
						if (getItemPrice(item) >= VALUE_FLAG) {
							items.add(item);
						}
					}
					for (Item item : player.getBank().generateContainer()) {
						if (item == null) {
							continue;
						}
						worth += getItemPrice(item);
						if (getItemPrice(item) >= VALUE_FLAG) {
							items.add(item);
						}
					}
					if (player.getFamiliar() != null) {
						if (player.getFamiliar().getBob() != null) {
							for (Item item : player.getFamiliar().getBob().getBeastItems().toArray()) {
								if (item == null) {
									continue;
								}
								worth += getItemPrice(item);
								if (getItemPrice(item) >= VALUE_FLAG) {
									items.add(item);
								}
							}
						}
					}
					if (worth > WEALTH_FLAG) {
						Map<Long, List<Item>> map = new HashMap<>();
						map.put(worth, items);
						VALUE_MAP.put(player.getUsername(), map);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ValueComparator bvc = new ValueComparator(VALUE_MAP);
		TreeMap<String, Map<Long, List<Item>>>  sorted_map = new TreeMap<>(bvc);
		sorted_map.putAll(VALUE_MAP);

		int number = 1;
		for (Entry<String, Map<Long, List<Item>>> entry : sorted_map.entrySet()) {
			Long networth = -1L;
			for (Entry<Long, List<Item>> listEntry : entry.getValue().entrySet()) {
				networth = listEntry.getKey();
				break;
			}
			String message = number + ".\t " + entry.getKey() + " has a networth of " + Utils.format(networth) + ". Rare items:\n";

			for (Entry<Long, List<Item>> listEntry : entry.getValue().entrySet()) {

				List<Item> itemList = listEntry.getValue();
				Collections.sort(itemList, (o1, o2) -> Integer.compare(getItemPrice(o2), getItemPrice(o1)));
				for (Item item : itemList) {
					message += "\t[" + item.getId() + "]" + item.getName() + " x" + Utils.format(item.getAmount()) + " - " + Utils.format(getItemPrice(item)) + "\n";
				}
			}
			new HiddenItemFinder().write(message);
			number++;
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
		return ExchangePriceLoader.getEconomicalPrice(item.getDefinitions().isNoted() ? item.getDefinitions().getCertId() : item.getId());
	}

	private static class ValueComparator implements Comparator<String> {

		private final Map<String, Map<Long, List<Item>>> base;

		public int compare(String a, String b) {
			Map<Long, List<Item>> aMap = base.get(a);
			Map<Long, List<Item>> bMap = base.get(b);
			Long firstValue = -1L;
			for (Entry<Long, List<Item>> entry : aMap.entrySet()) {
				firstValue = entry.getKey();
				break;
			}
			Long secondValue = -1L;
			for (Entry<Long, List<Item>> entry : bMap.entrySet()) {
				secondValue = entry.getKey();
				break;
			}
			return secondValue.compareTo(firstValue);
		}

		public ValueComparator(Map<String, Map<Long, List<Item>>> base) {
			this.base = base;
		}

	}

}
