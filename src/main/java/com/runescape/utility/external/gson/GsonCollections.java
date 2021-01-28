package com.runescape.utility.external.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Mar 1, 2014
 */
public abstract class GsonCollections<T> {

	/**
	 * Loads up all of the data
	 */
	public abstract void initialize();

	/**
	 * The gson instance
	 */
	protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Generates a list, if there are no elements in {@link #loadList()}, it will be null, so this will make sure there
	 * is no null return type from {@link #loadList()} ()}
	 */
	public List<T> generateList() {
		List<T> list = loadList();
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}

	/**
	 * Populates a list with all of the data in the {@link #getFileLocation()}
	 *
	 * @return The list with data
	 */
	public List<T> loadList() {
		throw new RuntimeException("You must override this method if you wish to use it.");
	/*	List<T> data = gson.fromJson(FileUtility.getText(getFileLocation()), new TypeToken<List<T>>() {
		}.getType());
		return data;*/
	}

	/**
	 * Generates a list, if there are no elements in {@link #loadMap()}}, it will be null, so this will make sure there
	 * is no null return type from {@link #loadMap()}}
	 */
	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> generateMap() {
		Map<K, V> map = (Map<K, V>) loadMap();
		if (map == null) {
			map = new HashMap<>();
		}
		return map;
	}

	/**
	 * Populates a list with all of the data in the {@link #getFileLocation()}
	 *
	 * @return The list with data
	 */
	public Map<?, ?> loadMap() {
		throw new RuntimeException("You must override this method if you wish to use it.");
	}

	/**
	 * Saves the data to the file
	 *
	 * @param data
	 * 		The list to save
	 */
	@SuppressWarnings("hiding")
	public <T> void save(T data) {
		try (Writer writer = new FileWriter(getFileLocation())) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC);
			Gson gson = builder.create();
			gson.toJson(data, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the location of the file to load from
	 */
	public abstract String getFileLocation();
}
