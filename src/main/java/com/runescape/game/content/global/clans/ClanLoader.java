package com.runescape.game.content.global.clans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * 
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 12, 2013
 */
public class ClanLoader {
	
	/**
	 * Starts the clans
	 */
	public static void initialize() {
		for (Clan clan : getLoadedList()) {
			clan.load();
			clans.add(clan);
		}
		System.out.println("Successfully loaded " + clans.size() + " game clans.");
	}
	
	public static void save(Clan clan) {
		List<Clan> clans = getLoadedList();
		ListIterator<Clan> it = clans.listIterator();
		while(it.hasNext()) {
			Clan clan2 = it.next();
			if (clan2.getClanName().equals(clan.getClanName()))
				it.remove();
		}
		clans.add(clan);
		save(clans);
		reload();
	}
	
	/**
	 * Gets the list of clans that are loaded, if there are no clans loaded, it creates a blank list.
	 * @return A {@code List} {@code Object}
	 */
	private static List<Clan> getLoadedList() {
		List<Clan> clans = load();
		if (clans == null)
			clans = new ArrayList<Clan>();
		return clans;
	}

	/**
	 * Gets the clan by the name
	 * 
	 * @param name
	 *            The name to look for
	 * @return A {@code Clan} {@code Object}
	 */
	public static Clan getClanByName(String name) {
		ListIterator<Clan> it = clans.listIterator();
		while (it.hasNext()) {
			Clan clan = it.next();
			if (clan.getClanName().equals(name))
				return clan;
		}
		return null;
	}

	public static void reload() {
		clans.clear();
		ListIterator<Clan> it = getLoadedList().listIterator();
		while(it.hasNext()) {
			Clan clan = it.next();
			clans.add(clan);
		}
	}
	
	public static void deleteClan(Clan clan) {
		List<Clan> clans = getLoadedList();
		ListIterator<Clan> it = clans.listIterator();
		while(it.hasNext()) {
			Clan clan2 = it.next();
			if (clan2.getClanName().equals(clan.getClanName()))
				it.remove();
		}
		save(clans);
		reload();
	}
	
	/**
	 * Adds a clan to the loaded list of clans and saves it. Also reloads the clans in the game.
	 * @param clan The clan to add
	 */
	public static void addClanAndSave(Clan clan) {
		List<Clan> clans = getLoadedList();
		clans.add(clan);
		save(clans);
		reload();
	} 
	
	/**
	 * Where the data will save/read from
	 * 
	 * @return An {@code String} object of the location
	 */
	private static String getFileLocation() {
		return "data/json/clans.json";
	}

	/**
	 * Populates a list with all of the data in the {@link #getFileLocation()}
	 * 
	 * @return The list with data
	 */
	public static List<Clan> load() {
		List<Clan> autospawns = null;
		String json = null;
		try {
			File file = new File(getFileLocation());
			if (!file.exists()) {
				return null;
			}
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			json = new String(chars);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		autospawns = gson.fromJson(json, new TypeToken<List<Clan>>() {
		}.getType());
		return autospawns;
	}

	/**
	 * Saves the list to the file
	 * 
	 * @param spawns
	 *            The list to save
	 */
	public static void save(List<Clan> spawns) {
		try {
			FileWriter fw = new FileWriter(getFileLocation());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(gson.toJson(spawns));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The gson instance
	 */
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The instance of the logger
	 */
	private static Logger logger = Logger.getLogger(ClanLoader.class.getName());
	
	/**
	 * The list of clans
	 */
	private static List<Clan> clans = new ArrayList<Clan>();

}
