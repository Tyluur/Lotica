package com.runescape.game.content;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles all operations that can be performed on potions, such as the mixing of two potions to form a
 * larger dose, and the forming of a flask from two potions.
 *
 * @author Tyluur <itstyluur@gmail.com>
 * @since August 3rd, 2013
 */
public final class PotionOperations {

	/** The price players pay per every single flask they make */
	private static final int PRICE_PER_FLASK_CONVERSION = 3_500;

	/**
	 * This method converts all potions in the player's inventory into flasks
	 *
	 * @param player
	 * 		The player
	 */
	public static void convertAllFlasks(Player player) {
		Map<String, Integer> dosesMap = getTotalFlaskDoses(player);
		for (Entry<String, Integer> entry : dosesMap.entrySet()) {
			String potionBaseName = entry.getKey();
			Integer totalDosesToMake = entry.getValue();
			for (Item item : player.getInventory().getItems().toArray()) {
				if (item == null || item.getDefinitions().isNoted()) {
					continue;
				}
				String name = item.getName();
				if (!isPotion(name)) {
					continue;
				}
				if (name.toLowerCase().contains(potionBaseName.toLowerCase())) {
					player.getInventory().deleteItem(item);
				}
			}
			int flaskId = createFlask(potionBaseName);
			if (flaskId == -1) {
				System.out.println("Couldn't find flask for name:\t" + potionBaseName);
				continue;
			}
			int amountToMake = Math.floorDiv(totalDosesToMake, 6);
			int remainder = totalDosesToMake % 6;
			player.getInventory().addItem(flaskId, amountToMake);
			if (remainder != 0) {
				int potionId = createPotion(potionBaseName, remainder);
				if (potionId != -1) {
					player.getInventory().addItem(potionId, 1);
				}
			}

			if (GameConstants.DEBUG) {
				System.out.println("amountToMake:" + amountToMake + ", remainder=" + remainder);
			}
		}
	}

	/**
	 * This method finds the id of a full dose flask from the base name
	 *
	 * @param baseName
	 * 		The base name of the potion
	 */
	private static int createFlask(String baseName) {
		String newName = baseName + " flask (6)";
		ItemDefinitions definitions = ItemDefinitions.forName(newName);
		if (definitions == null) {
			return -1;
		}
		return definitions.getId();
	}

	/**
	 * This method finds the id of a potion with the supplied parameters
	 *
	 * @param baseName
	 * 		The base name of the potion
	 * @param doses
	 * 		The amount of doses of the potion
	 */
	private static int createPotion(String baseName, int doses) {
		String newName = baseName + " (" + doses + ")";
		ItemDefinitions definitions = ItemDefinitions.forName(newName);
		if (definitions == null) {
			return -1;
		}
		return definitions.getId();
	}


	/**
	 * This method constructs a map, using potion base names as the key and the amount of doses for that potion as the
	 * value. We use this data in the conversion process of potions -> flasks
	 *
	 * @param player
	 * 		The player we're getting the data from
	 */
	public static Map<String, Integer> getTotalFlaskDoses(Player player) {
		Map<String, Integer> map = new HashMap<>();
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null || item.getDefinitions().isNoted()) {
				continue;
			}
			if (!isPotion(item.getName())) {
				continue;
			}
			String baseName = getBaseName(item.getName());
			Integer doses = map.get(baseName);
			String[] itemNameSplit = item.getName().split(" ");
			if (doses == null) {
				doses = getPotionDoses(itemNameSplit);
			} else {
				doses += getPotionDoses(itemNameSplit);
			}
			map.put(baseName, doses);
		}
		Iterator<Entry<String, Integer>> it$ = map.entrySet().iterator();
		while (it$.hasNext()) {
			Entry<String, Integer> entry = it$.next();
			if (entry.getValue() < 6) {
				it$.remove();
			}
		}
		return map;
	}

	/**
	 * Gets the total amount of flasks that we can make
	 *
	 * @param player
	 * 		The player doing the conversion
	 */
	public static int getTotalFlasksCreateable(Player player) {
		Map<String, Integer> dosesMap = getTotalFlaskDoses(player);
		int doses = 0;
		for (Entry<String, Integer> entry : dosesMap.entrySet()) {
			Integer value = entry.getValue();
			doses += value / 6;
		}
		return doses;
	}

	/**
	 * Gets the cost to convert the potions into flasks
	 *
	 * @param player
	 * 		The player converting the potions to flasks
	 */
	public static int getFlaskConversionCost(Player player) {
		int totalFlasksCreateable = getTotalFlasksCreateable(player);
		if (totalFlasksCreateable <= 0) {
			return 0;
		} else {
			return totalFlasksCreateable * PRICE_PER_FLASK_CONVERSION;
		}
	}

	/**
	 * Handles the decantation of all potions in the player's inventory, to the specified dosage
	 *
	 * @param player
	 * 		The player decanting
	 * @param maxDosage
	 * 		The dosage to decant potions to
	 */
	public static void decantInventory(Player player, int maxDosage) {
		Map<String, List<Item>> map = getDecantablePotions(player);
		Iterator<Entry<String, List<Item>>> it$ = map.entrySet().iterator();
		List<Item> newItems = new ArrayList<>();
		List<Item> items = null;
		List<Item> inventoryItems = new ArrayList<>();
		while (it$.hasNext()) {
			Entry<String, List<Item>> entry = it$.next();
			String baseName = entry.getKey();
			items = entry.getValue();
			int totalDoses = 0;
			for (Item item : items) {
				inventoryItems.add(item);
				totalDoses += getPotionDoses(item.getName().split(" "));
			}
			List<Integer> doses = new ArrayList<>();
			int tempDose = totalDoses;
			for (int i = tempDose; i > 0; i--) {
				if (i % maxDosage == 0) {
					tempDose -= 4;
					doses.add(maxDosage);
				} else if (i == 1 && tempDose > 0) {
					doses.add(tempDose);
				}
			}
			for (Integer dose : doses) {
				String newName = baseName + " (" + dose + ")";
				ItemDefinitions definitions = ItemDefinitions.forName(newName);
				if (definitions == null) {
					continue;
				}
				int newId = definitions.getId();
				ItemDefinitions defs = ItemDefinitions.getItemDefinitions(newId);
				if (defs.isNoted() && defs.getCertId() != -1) {
					newId = defs.getCertId();
				}
				newItems.add(new Item(newId, 1));
			}
		}
		if (items == null) {
			return;
		}
		for (Item item : items) {
			if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
				return;
			}
		}
		int nSize = newItems.size();
		int size = items.size();
		if (nSize < size) {
			int difference = size - nSize;
			for (int i = 0; i < difference; i++) {
				newItems.add(new Item(229));
			}
		}
		for (Item item : inventoryItems) {
			player.getInventory().deleteItem(item);
		}
		for (Item item : newItems) {
			player.getInventory().addItem(item);
		}
	}

	/**
	 * Creates a map of all decantable potions and stores the base name of the potions as the key (e.g super strength).
	 * The list of all potions that can be decanted is the value of the map.
	 *
	 * @param player
	 * 		The player
	 */
	public static Map<String, List<Item>> getDecantablePotions(Player player) {
		Map<String, List<Item>> map = new HashMap<>();
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null || item.getDefinitions().isNoted()) {
				continue;
			}
			String name = item.getName();
			if (isPotion(name)) {
				String baseName = getBaseName(name);
				if (!map.containsKey(baseName)) {
					List<Item> items = new ArrayList<>();
					items.add(item);
					map.put(baseName, items);
				} else {
					List<Item> items = map.get(baseName);
					items.add(item);
				}
			}
		}
		return map;
	}

	/**
	 * Gets the cost of decanting all the potions in a player's inventory
	 *
	 * @param player
	 * 		The player
	 */
	public static int getPotionCost(Player player) {
		Map<String, List<Item>> map = PotionOperations.getDecantablePotions(player);
		Iterator<Entry<String, List<Item>>> it$ = map.entrySet().iterator();
		int count = 0;
		while (it$.hasNext()) {
			Entry<String, List<Item>> entry = it$.next();
			List<Item> items = entry.getValue();
			int totalDoses = 0;
			for (Item item : items) {
				totalDoses += getPotionDoses(item.getName().split(" "));
			}
			count += (totalDoses / 4);
		}
		count *= COST_PER_POTION;
		return count;
	}

	/**
	 * Figures out if you can use the two potions on each other
	 *
	 * @param first
	 * 		The name of the first potion used
	 * @param second
	 * 		The name of the second potion used
	 */
	private static boolean isPotionSimilar(String first, String second) {
		String firstBase = getBaseName(first);
		String secondBase = getBaseName(second);
		return firstBase.equalsIgnoreCase(secondBase);
	}

	/**
	 * Gets the doses of the potion
	 *
	 * @param name
	 * 		The name of the potion split by spaces
	 */
	private static int getPotionDoses(String[] name) {
		int doses = 0;
		for (String split : name) {
			for (Character c : split.toCharArray()) {
				if (Character.isDigit(c)) {
					doses = Character.getNumericValue(c);
					break;
				}
			}
		}
		return doses;
	}

	/**
	 * Gets the base name of the potion
	 *
	 * @param name
	 * 		The name of the potion
	 */
	private static String getBaseName(String name) {
		String newName = "";
		for (Character c : name.toCharArray()) {
			if (Character.isLetter(c) || Character.isWhitespace(c)) {
				newName += c;
			}
		}
		return newName.trim();
	}

	/**
	 * Figures out if the item you are using is a potion or not.
	 *
	 * @param name
	 * 		The name of the item
	 */
	private static boolean isPotion(String name) {
		if (name.toLowerCase().contains("amulet") || name.toLowerCase().contains("games") || name.toLowerCase().contains("flask")) {
			return false;
		}
		String altString = "";
		for (Character c : name.toCharArray()) {
			if (Character.isLetter(c) || Character.isWhitespace(c)) {
				continue;
			}
			altString += c;
		}
		Pattern pattern = Pattern.compile("\\([0-9]\\)");
		Matcher m = pattern.matcher(altString);
		return m.find();
	}

	/**
	 * Handles the decantation process of two like potions
	 *
	 * @param player
	 * 		The player who is decanting
	 * @param fromSlot
	 * 		The slot the first item is from
	 * @param toSlot
	 * 		The slot the second item is from
	 */
	public static boolean handleDecanting(Player player, int fromSlot, int toSlot) {
		try {
			Item used = player.getInventory().getItem(toSlot);
			Item with = player.getInventory().getItem(fromSlot);
			String usedBase = used.getName();
			String withBase = with.getName();
			if (!isPotion(usedBase) || !isPotion(withBase) || !isPotionSimilar(usedBase, withBase)) {
				return false;
			}
			String baseName = getBaseName(usedBase);
			String[] usedSplit = usedBase.split(" ");
			String[] withSplit = withBase.split(" ");
			int usedDoses = getPotionDoses(usedSplit);
			int withDoses = getPotionDoses(withSplit);
			int totalDoses = usedDoses + withDoses;
			int[] newDoses = new int[2];
			if (totalDoses > 4) {
				newDoses[0] = 4;
				newDoses[1] = totalDoses - 4;
			} else {
				newDoses[0] = totalDoses;
			}
			if (totalDoses == 8) {
				return true;
			}
			int[] prevDoses = new int[] { usedDoses, withDoses };
			if (arraysEqual(newDoses, prevDoses)) {
				player.sendMessage("Nothing interesting happens.");
				return true;
			}
			player.getInventory().deleteItem(with);
			player.getInventory().deleteItem(used);
			for (int dose : newDoses) {
				if (dose == 0) {
					player.getInventory().addItem(new Item(229));
					continue;
				}
				String newName = baseName + " (" + dose + ")";
				ItemDefinitions definitions = ItemDefinitions.forName(newName);
				if (definitions == null) {
					continue;
				}
				int newId = definitions.getId();
				ItemDefinitions defs = ItemDefinitions.getItemDefinitions(newId);
				if (defs.isNoted() && defs.getCertId() != -1) {
					newId = defs.getCertId();
				}
				Item item = new Item(newId, 1);
				player.getInventory().addItem(item);
			}
			if (GameConstants.DEBUG) {
				StringBuilder bldr = new StringBuilder();
				for (int dose : newDoses) {
					bldr.append(dose).append(", ");
				}
				System.out.println("Dosage info[baseName=" + baseName + ", total=" + totalDoses + ", usedDoses=" + usedDoses + ", withDoses=" + withDoses + ", newDoses=" + bldr.toString().trim() + "]");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean arraysEqual(int[] array1, int[] array2) {
		if (array1.length != array2.length) {
			return false;
		}
		for (int array1Number : array1) {
			boolean inOtherArray = false;
			for (int array2Number : array2) {
				if(array2Number == array1Number) {
					inOtherArray = true;
				}
			}
			if (!inOtherArray) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The cost to decant one potion
	 */
	public static final int COST_PER_POTION = 1000;

}