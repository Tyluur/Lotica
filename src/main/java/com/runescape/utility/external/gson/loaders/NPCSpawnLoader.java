package com.runescape.utility.external.gson.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.runescape.cache.Cache;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.resource.NPCSpawn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 15, 2015
 */
public class NPCSpawnLoader {

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();
		File file = new File("C:\\Users\\Tyler\\Dropbox\\Lotica\\Live Server\\data\\resource\\world\\npcs\\spawns\\");
		File[] spawns = file.listFiles();
		for (File spawnFile : spawns) {
			int regionId = Integer.parseInt(spawnFile.getName().replaceAll(".json", ""));
			List<NPCSpawn> spawnList = loadFromFile(regionId);
			boolean changed = false;
			for (Iterator<NPCSpawn> iterator = spawnList.iterator(); iterator.hasNext(); ) {
				NPCSpawn spawn = iterator.next();
				if (spawn.getNpcId() >= Utils.getNPCDefinitionsSize()) {
					iterator.remove();
					System.out.println("Removed npc: " + spawn.getNpcId());
					changed = true;
				}
			}
			if (changed) {
				saveData(regionId, spawnList);
			}
		}
	}

	/**
	 * Adds a spawn to the list of spawns and saves it
	 *
	 * @param npcId
	 * 		The id of the spawn
	 * @param tile
	 * 		The tile of the spawn
	 * @param direction
	 * 		The direction of the spawn
	 */
	public static void addSpawn(int npcId, WorldTile tile, Direction direction) {
		List<NPCSpawn> spawns = loadFromFile(tile.getRegionId());
		if (spawns == null) {
			spawns = new ArrayList<>();
		}
		spawns.add(new NPCSpawn(npcId, tile, direction));
		saveData(tile.getRegionId(), spawns);
		World.spawnNPC(npcId, tile, -1, true, direction);
		System.out.println("Spawned " + npcId + " on " + tile + " facing " + direction + " at region " + tile.getRegionId());
	}

	/**
	 * Removes an npc spawn
	 *
	 * @param npc
	 * 		The npc to remove the spawn for
	 */
	public static boolean removeSpawn(NPC npc) {
		List<NPCSpawn> spawns = loadFromFile(npc.getRegionId());
		if (spawns == null) {
			return false;
		}
		boolean removed = false;
		Iterator<NPCSpawn> it$ = spawns.iterator();
		while (it$.hasNext()) {
			NPCSpawn spawn = it$.next();
			if (spawn.getNpcId() == npc.getId() && spawn.getTile().matches(npc.getRespawnTile())) {
				it$.remove();
				removed = true;
			}
		}
		if (removed) {
			saveData(npc.getRegionId(), spawns);
			System.out.println("Removed npc and saved file!\t" + npc);
		}
		return removed;
	}

	/**
	 * Loads all of the {@link NPCSpawn}s of the region into the world
	 *
	 * @param regionId
	 * 		The region to find the spawns of
	 */
	public static void loadSpawns(int regionId) {
		if (!regionSpawnsExist(regionId)) {
			return;
		}
		List<NPCSpawn> spawns = loadFromFile(regionId);
		if (spawns == null) {
			return;
		}
		spawns.forEach(spawn -> World.spawnNPC(spawn.getNpcId(), spawn.getTile(), -1, true, spawn.getDirection()));
	}

	/**
	 * Loads and constructs a new {@code NPCSpawn} {@code List} from the {@link #getFileLocation(int)} for the region
	 * id
	 *
	 * @param regionId
	 * 		The id of the reigon
	 */
	private static List<NPCSpawn> loadFromFile(int regionId) {
		File file = new File(getFileLocation(regionId));
		if (!file.exists()) {
			return null;
		}
		return GSON.fromJson(Utils.getText(file.getAbsolutePath()), new TypeToken<List<NPCSpawn>>() {
		}.getType());
	}

	/**
	 * Saves the data to a file
	 *
	 * @param regionId
	 * 		The region of the spawns
	 * @param spawns
	 * 		The spawn data to write
	 */
	public static void saveData(int regionId, List<NPCSpawn> spawns) {
		try (Writer writer = new FileWriter(getFileLocation(regionId))) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();
			gson.toJson(spawns, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if there are spawns for the region
	 *
	 * @param regionId
	 * 		The region id to check for
	 */
	private static boolean regionSpawnsExist(int regionId) {
		return new File(getFileLocation(regionId)).exists();
	}

	/**
	 * @param regionId
	 * 		The id of the region
	 */
	private static String getFileLocation(int regionId) {
		return DATA_LOCATION + regionId + ".json";
	}

	public enum Direction {

		NORTH(0),
		NORTHEAST(1),
		EAST(2),
		SOUTHEAST(3),
		SOUTH(4),
		SOUTHWEST(5),
		WEST(6),
		NORTHWEST(7);

		private int value;

		Direction(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Direction getDirection(String text) {
			for (Direction d : Direction.values()) {
				if (d.name().equalsIgnoreCase(text)) {
					return d;
				}
			}
			return null;
		}
	}

	/**
	 * The gson instance
	 */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The location that data will be stored
	 */
	private static final String DATA_LOCATION = "./data/resource/world/npcs/spawns/";

}
