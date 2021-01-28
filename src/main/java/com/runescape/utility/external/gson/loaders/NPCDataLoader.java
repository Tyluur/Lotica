package com.runescape.utility.external.gson.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.world.entity.npc.Drop;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemProperties;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.resource.NPCData;
import com.runescape.workers.game.core.CoresManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class NPCDataLoader {

	/**
	 * The default npc definition
	 */
	public final static NPCCombatDefinitions DEFAULT_DEFINITION = new NPCCombatDefinitions(1, -1, -1, -1, 5, 1, 33, 0, NPCCombatDefinitions.MELEE, -1, -1, NPCCombatDefinitions.PASSIVE);

	/**
	 * The map of cached data
	 */
	public static final Map<String, NPCData> CACHED_DATA = new HashMap<>();

	/**
	 * The gson instance
	 */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The location that data will be stored
	 */
	public static final String DATA_LOCATION = "./data/resource/world/npcs/data/";

	/**
	 * The lock to synchronize through
	 */
	private static final Object LOCK = new Object();

	public static void main(String[] args) throws IOException {
/*		Cache.init();


		List<String> text = Utils.getFileText(System.getProperty("user.home") + "/Desktop/Drop table.txt");

		List<Integer> resetDrops = new ArrayList<>();

		for (String line : text) {
			int openIndex = line.indexOf("[") + 1;
			int closeIndex = line.indexOf("]");
			int startItemIndex = closeIndex + 2;
			String npcIdString = line.substring(openIndex, closeIndex).trim();
			int npcId = Integer.parseInt(npcIdString);
			String definitions = line.substring(startItemIndex, line.length()).trim();

			String npcName = NPCDefinitions.getNPCDefinitions(npcId).getName();
			NPCData data = getData(npcName);
			if (data == null) {
				System.out.println("Could not find data for " + npcName);
				continue;
			}
			String[] split = definitions.split(", ");
			List<Drop> drops = data.getDrops();
			if (!resetDrops.contains(npcId)) {
				drops = new ArrayList<>();
				resetDrops.add(npcId);
				System.out.println("Reset the drops of " + npcName);
			}
			int itemId = Integer.parseInt(split[0]);
			double rate = Double.parseDouble(split[1]);
			int minAmount = Integer.parseInt(split[2]);
			int maxAmount = Integer.parseInt(split[3]);

			Drop drop = new Drop(itemId, rate, minAmount, maxAmount);
			drops.add(drop);
			System.out.println("Added drop (" + ItemDefinitions.getItemDefinitions(drop.getItemId()).getName() + ") " + drop + " to " + npcName);

			saveData(npcName, data);
		}
		System.exit(-1);
*/

//		addNPCBonuses(83, new int[] { 125, 125, 125, 0, 0, 95, 95, 95, 63, 105 });

		/*

		int id = 5904;
		// the untouchable
		addNPCBonuses(id, new int[] { 300, 0, 0, 0, 0, 200, 200, 200, 200, 200 });
		addNPCCombatDefinitions(id, new NPCCombatDefinitions(1700, 6329, 6330, 6331, 6, 4, 50, 190, NPCCombatDefinitions.MELEE, -1, -1, NPCCombatDefinitions.PASSIVE));
		// the everlasting
		id = 5903;
		addNPCBonuses(id, new int[] { 250, 0, 0, 0, 0, 200, 200, 200, 200, 200 });
		addNPCCombatDefinitions(id, new NPCCombatDefinitions(2300, 6345, 6346, 6347, 6, 4, 50, 240, NPCCombatDefinitions.MELEE, -1, -1, NPCCombatDefinitions.PASSIVE));
		// The Inadequacy
		id = 5902;
		addNPCBonuses(id, new int[] { 500, 0, 0, 0, 0, 500, 500, 500, 100, 500 });
		addNPCCombatDefinitions(id, new NPCCombatDefinitions(1800, 6325, 6324, 6322, 5, 4, 50, 200, NPCCombatDefinitions.MELEE, -1, -1, NPCCombatDefinitions.PASSIVE));
		// barrelchest
		id = 5666;
		addNPCBonuses(id, new int[] { 250, 0, 0, 0, 250, 140, 140, 140, 250, 250 });
		addNPCCombatDefinitions(id, new NPCCombatDefinitions(1340, 5894, 5896, 5898, 5, 8, 50, 350, NPCCombatDefinitions.MELEE, -1, -1, NPCCombatDefinitions.PASSIVE));
		// evil chicken
		id = 3375;
		addNPCBonuses(id, new int[] { 0, 0, 0, 190, 0, 112, 112, 112, 140, 112 });
		addNPCCombatDefinitions(id, new NPCCombatDefinitions(1200, 2302, 2299, 2301, 5, 2, 60, 50, NPCCombatDefinitions.MAGE, -1, -1, NPCCombatDefinitions.PASSIVE));
*/

		Cache.init();
		CoresManager.init();
		GsonStartup.loadAll();
		File[] files = new File(DATA_LOCATION).listFiles();
		assert files != null;
		for (File file : files) {
			if (!file.getName().startsWith("Revenant")) {
				continue;
			}
			String npcName = file.getName().replaceAll(".json", "");
			NPCData data = getData(npcName);
			if (data == null) {
				continue;
			}
			List<Drop> drops = data.getDrops();
			boolean changed = false;
			for (Iterator<Drop> iterator = drops.iterator(); iterator.hasNext(); ) {
				Drop drop = iterator.next();

				String name = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName();
				String[] flags = { "c.", "corrupt", "morrigan", "statius", "zuriel", "vesta" };
				for (String flag : flags) {
					if (name.toLowerCase().contains(flag.toLowerCase())) {
						/*System.out.println("Removed [" + name + "]" + drop + " from " + npcName);
						iterator.remove();*/
						boolean corrupt = name.toLowerCase().contains("corrupt") || name.toLowerCase().contains("c.");
						boolean thrown = name.toLowerCase().contains("javelin") || name.toLowerCase().contains("throwing");
						drop.setRate(thrown ? 7 : corrupt ? 5 : 1.5);
						System.out.println("Set drop " + (ItemDefinitions.getItemDefinitions(drop.getItemId()).getName()) + " [" + drop + "]");
						changed = true;
					}
				}
			}
			if (changed) {
				saveData(npcName, data);
			}
		}
		System.exit(-1);
	}

	private static void convertDefinitions(String line) {
		String[] splitedLine = line.split(" - ", 2);
		if (splitedLine.length != 2) {
			throw new RuntimeException("Invalid NPC Combat Definitions line: " + line);
		}
		int npcId = Integer.parseInt(splitedLine[0]);
		String[] splitedLine2 = splitedLine[1].split(" ", 12);
		if (splitedLine2.length != 12) {
			throw new RuntimeException("Invalid NPC Combat Definitions line: " + line);
		}
		int hitpoints = Integer.parseInt(splitedLine2[0]);
		int attackAnim = Integer.parseInt(splitedLine2[1]);
		int defenceAnim = Integer.parseInt(splitedLine2[2]);
		int deathAnim = Integer.parseInt(splitedLine2[3]);
		int attackDelay = Integer.parseInt(splitedLine2[4]);
		int deathDelay = Integer.parseInt(splitedLine2[5]);
		int respawnDelay = Integer.parseInt(splitedLine2[6]);
		int maxHit = Integer.parseInt(splitedLine2[7]);
		int attackStyle;
		if (splitedLine2[8].equalsIgnoreCase("MELEE")) {
			attackStyle = NPCCombatDefinitions.MELEE;
		} else if (splitedLine2[8].equalsIgnoreCase("RANGE")) {
			attackStyle = NPCCombatDefinitions.RANGE;
		} else if (splitedLine2[8].equalsIgnoreCase("MAGE")) {
			attackStyle = NPCCombatDefinitions.MAGE;
		} else if (splitedLine2[8].equalsIgnoreCase("SPECIAL")) {
			attackStyle = NPCCombatDefinitions.SPECIAL;
		} else if (splitedLine2[8].equalsIgnoreCase("SPECIAL2")) {
			attackStyle = NPCCombatDefinitions.SPECIAL2;
		} else {
			throw new RuntimeException("Invalid NPC Combat Definitions line: " + line);
		}
		int attackGfx = Integer.parseInt(splitedLine2[9]);
		int attackProjectile = Integer.parseInt(splitedLine2[10]);
		int agressivenessType;
		if (splitedLine2[11].equalsIgnoreCase("PASSIVE")) {
			agressivenessType = NPCCombatDefinitions.PASSIVE;
		} else if (splitedLine2[11].equalsIgnoreCase("AGRESSIVE")) {
			agressivenessType = NPCCombatDefinitions.AGRESSIVE;
		} else {
			throw new RuntimeException("Invalid NPC Combat Definitions line: " + line);
		}
		addNPCCombatDefinitions(npcId, new NPCCombatDefinitions(hitpoints, attackAnim, defenceAnim, deathAnim, attackDelay, deathDelay, respawnDelay, maxHit, attackStyle, attackGfx, attackProjectile, agressivenessType));
	}

	/**
	 * Adds npc bonuses to the data for the npc
	 *
	 * @param npcId
	 * 		The npc id
	 * @param bonuses
	 * 		The bonuses for the npc
	 */
	public static void addNPCBonuses(int npcId, int[] bonuses) {
		String name = NPCDefinitions.getNPCDefinitions(npcId).getName();
		NPCData data = getData(name);
		if (data == null) {
			data = new NPCData();
		}
		data.getBonuses().put(npcId, bonuses);
		saveData(name, data);
		System.out.println("Stored bonuses(" + Arrays.toString(bonuses) + ")\tfor: " + NPCDefinitions.getNPCDefinitions(npcId).getName() + "[" + npcId + "]");
	}

	/**
	 * Adds drops to the npc
	 *
	 * @param key
	 * 		The key of the npc, either an integer of a string
	 * @param drops
	 * 		The dropsd
	 */
	public static void addNPCDrops(Object key, boolean charms, Drop... drops) {
		String name = (key instanceof String ? (String) key : key instanceof Integer ? (NPCDefinitions.getNPCDefinitions((Integer) key).getName()) : null);
		if (name == null) {
			throw new IllegalStateException("Couldn't generate npc info from key=" + key);
		}
		NPCData data = getData(name);
		if (data == null) {
			data = new NPCData();
		}
		List<Drop> dropList = Arrays.asList(drops);
		if (charms) { data.getCharmDrops().addAll(dropList); } else { data.getDrops().addAll(dropList); }
		saveData(name, data);
		System.out.println("Added " + dropList.size() + " " + (charms ? "charms" : "drops") + " for " + name);
	}

	/**
	 * Gets the data of an npc
	 *
	 * @param name
	 * 		The name of the npc
	 */
	public static NPCData getData(String name) {
		synchronized (LOCK) {
			if (!hasDataForNPC(name)) {
				return null;
			}
			NPCData drop = CACHED_DATA.get(name);
			if (drop == null) {
				CACHED_DATA.put(name, drop = loadFromFile(name));
			}
			return drop;
		}
	}

	/**
	 * Saves the data to a file
	 *
	 * @param name
	 * 		The name
	 * @param data
	 * 		The data
	 */
	public static void saveData(String name, NPCData data) {
		try (Writer writer = new FileWriter(getFileLocation(name))) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();
			gson.toJson(data, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If we have the data for an npc
	 *
	 * @param name
	 * 		The name of the npc
	 */
	private static boolean hasDataForNPC(String name) {
		return new File(getFileLocation(name)).exists();
	}

	/**
	 * Loads and constructs a new {@code NPCDrop} from the {@link #getFileLocation(String)} for the npc name
	 *
	 * @param name
	 * 		The name of the npc
	 */
	private static NPCData loadFromFile(String name) {
		String text = Utils.getText(getFileLocation(name));
		return GSON.fromJson(text, NPCData.class);
	}

	private static void convertMatrix3Drops() {
		String text = "20429, 1, 2, 0\n" +
				              "1127, 1, 1, 1\n" +
				              "1079, 1, 1, 1\n" +
				              "4091, 1, 1, 1\n" +
				              "4093, 1, 1, 1\n" +
				              "4091, 1, 1, 1\n" +
				              "4093, 1, 1, 1\n" +
				              "3054, 1, 1, 1\n" +
				              "1319, 1, 1, 3\n" +
				              "1373, 1, 1, 3\n" +
				              "1149, 1, 1, 4\n" +
				              "4699, 50, 950, 1\n" +
				              "565, 50, 100, 1\n" +
				              "560, 50, 50, 1\n" +
				              "892, 100, 100, 1\n" +
				              "9244, 100, 100, 1\n" +
				              "5292, 1, 1, 1\n" +
				              "5311, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "2436, 1, 1, 2\n" +
				              "2440, 1, 1, 2\n" +
				              "2442, 1, 1, 2\n" +
				              "2434, 1, 1, 2\n" +
				              "1623, 1, 8, 2\n" +
				              "385, 2, 10, 1\n" +
				              "-1, 1, 1, 1\n" +
				              "2364, 1, 15, 2";
		int[] rates = { 100, 90, 75, 2, 1 };
		List<Drop> drops = new ArrayList<>();
		for (String line : text.split("\n")) {
			String[] data = line.split(",");
			for (int i = 0; i < data.length; i++) {
				data[i] = data[i].trim();
			}
			int itemId = Integer.parseInt(data[0]);
			if (itemId == -1) { continue; }
			int minAmount = Integer.parseInt(data[1]);
			int maxAmount = Integer.parseInt(data[2]);
			int chance = Integer.parseInt(data[3]);
			int realRate = rates[chance];
			Drop drop = new Drop(itemId, realRate, minAmount, maxAmount);
			System.out.println("Created new drop:\t" + drop);
			drops.add(drop);
		}
		setNPCDrops(3334, drops);
	}

	/**
	 * @param name
	 * 		The name of the npc
	 */
	private static String getFileLocation(String name) {
		return DATA_LOCATION + name + ".json";
	}

	/**
	 * This method checks the values of all items to ensure economic safety
	 */
	private static void checkItemValues() throws IOException {
		Cache.init();
		ItemProperties.loadProperties();
		int priceToCheck = 1_000_000;
		int minAmountToCheck = 10_000;
		int maxAmountToCheck = 20_000;

		List<String> namesChecked = new ArrayList<>();

		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
			if (defs == null) {
				System.err.println("No definitions for npc: " + i);
				continue;
			}
			String name = defs.getName();
			if (namesChecked.contains(name)) {
				continue;
			}

			NPCData data = getData(name);
			if (data == null) {
				continue;
			}
			List<Drop> drops = data.getDrops();
			for (Drop drop : drops) {
				int itemId = drop.getItemId();
				String itemName = ItemDefinitions.getItemDefinitions(itemId).getName();

				int minAmount = drop.getMinAmount();
				int maxAmount = drop.getMaxAmount();
				int extraAmount = drop.getExtraAmount();

				Item minItem = new Item(itemId, minAmount + extraAmount);
				Item maxItem = new Item(itemId, maxAmount + extraAmount);

				int minPrice = minItem.getDefinitions().getExchangePrice() * minItem.getAmount();
				int maxPrice = maxItem.getDefinitions().getExchangePrice() * maxItem.getAmount();

				if (minAmount >= minAmountToCheck) {
					System.out.println(name + ":\t" + drop + "\t->\t[name=" + itemName + "]");
				}

				if (maxAmount >= maxAmountToCheck) {
					System.out.println(name + ":\t" + drop + "\t->\t[name=" + itemName + "]");
				}

				if (minPrice > priceToCheck) {
					System.out.println(name + ":\t" + drop + "\t->\t[name=" + itemName + ", minPrice=" + Utils.format(minPrice) + "]");
				}
				if (maxPrice > priceToCheck) {
					System.out.println(name + ":\t" + drop + "\t->\t[name=" + itemName + ", maxPrice=" + Utils.format(maxPrice) + "]");
				}
			}
			namesChecked.add(name);
		}
	}

	/**
	 * Adds combat definitions to the npc
	 *
	 * @param npcId
	 * 		The npc id
	 * @param definitions
	 * 		The definitions
	 */
	public static void addNPCCombatDefinitions(int npcId, NPCCombatDefinitions definitions) {
		String name = NPCDefinitions.getNPCDefinitions(npcId).getName();
		NPCData data = getData(name);
		if (data == null) {
			data = new NPCData();
		}
		data.getCombatDefinitions().put(npcId, definitions);
		saveData(name, data);
		System.out.println("Stored combat definitions for: " + NPCDefinitions.getNPCDefinitions(npcId).getName() + "[" + npcId + "]");
	}

	public static void setNPCDrops(int npcId, List<Drop> drops) {
		String name = NPCDefinitions.getNPCDefinitions(npcId).getName();
		NPCData data = getData(name);
		if (data == null) {
			data = new NPCData();
		}
		data.getDrops().clear();
		data.getDrops().addAll(drops);
		saveData(name, data);
		System.out.println("Stored drops for: " + NPCDefinitions.getNPCDefinitions(npcId).getName() + "[" + npcId + "]");
	}

	/**
	 * Gets the bonuses of an npc by its id
	 *
	 * @param npcId
	 * 		The npc id
	 */
	public static int[] getBonuses(int npcId) {
		String name = NPCDefinitions.getNPCDefinitions(npcId).getName();
		NPCData data = getData(name);
		if (data == null) {
			return null;
		}
		return data.getBonuses().get(npcId);
	}

	/**
	 * Gets the combat definitions of an npc by its id
	 *
	 * @param npcId
	 * 		The npc id
	 */
	public static NPCCombatDefinitions getCombatDefinitions(int npcId) {
		String name = NPCDefinitions.getNPCDefinitions(npcId).getName();
		NPCData data = getData(name);
		if (data == null) {
			return DEFAULT_DEFINITION;
		}
		NPCCombatDefinitions definitions = data.getCombatDefinitions().get(npcId);
		if (definitions == null) {
			return DEFAULT_DEFINITION;
		}
		return definitions;
	}

	/**
	 * This method constructs a large map of all npc data via the files in the {@link #DATA_LOCATION} directory This
	 * method should only be used once to cache all data as the operation is lengthy.
	 */
	public static Map<String, NPCData> getAllNPCData() {
		Map<String, NPCData> npcData = new HashMap<>();
		File[] dataFiles = new File(DATA_LOCATION).listFiles();
		for (File dataFile : dataFiles) {
			String name = dataFile.getName().replaceAll(".json", "");
			NPCData data = getData(name);
			if (data == null) {
				System.out.println("No data for file:\t" + name);
				continue;
			}
			npcData.put(name, data);
		}
		return npcData;
	}

	/**
	 * Deletes the data file
	 *
	 * @param name
	 * 		The name of the file
	 */
	public static boolean deleteDataFile(String name) {
		File file = new File(getFileLocation(name));
		return file.delete();
	}
}
