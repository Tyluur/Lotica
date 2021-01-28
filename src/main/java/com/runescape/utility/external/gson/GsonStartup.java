package com.runescape.utility.external.gson;

import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.utility.external.gson.loaders.*;

import java.util.*;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class GsonStartup {

	/**
	 * Adds all gson loaders to the list to load
	 */
	public static void loadAll() {
		loaders.add(new StoreLoader());
		loaders.add(new PunishmentLoader());
		loaders.add(new LentItemsLoader());
		loaders.add(new DefaultPresetsLoader());
		loaders.add(new RightManager());
		loaders.add(new ExchangeItemLoader());
		loaders.add(new ExchangePriceLoader());
		loaders.forEach(GsonCollections::initialize);
	}

	/**
	 * Gets the class for a loader
	 *
	 * @param clazz
	 * 		The class of the loader
	 */
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> getOptional(Class<T> clazz) {
		GsonCollections<T> loader = getLoaderClass(clazz);
		if (loader == null) {
			return Optional.empty();
		}
		return (Optional<T>) Optional.of(loader);
	}
	
	/**
	 * Gets the class for a loader
	 *
	 * @param clazz
	 * 		The class of the loader
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getClass(Class<T> clazz) {
		GsonCollections<T> loader = getLoaderClass(clazz);
		if (loader == null) {
			throw new IllegalStateException("Couldn't find GsonCollection loader for class:\t" + clazz);
		}
		return (T) (loader);
	}

	/**
	 * Gets the {@link GsonCollections} class instance of the class
	 *
	 * @param clazz
	 * 		The class
	 */
	@SuppressWarnings("unchecked")
	private static <T> GsonCollections<T> getLoaderClass(Class<T> clazz) {
		if (CACHED_CLASSES.get(clazz.getName()) != null) {
			return (GsonCollections<T>) CACHED_CLASSES.get(clazz.getName());
		}
		for (GsonCollections<?> loader : loaders) {
			if (loader.getClass().equals(clazz)) {
				CACHED_CLASSES.put(clazz.getName(), loader);
				return (GsonCollections<T>) loader;
			}
		}
		throw new IllegalStateException("Couldn't find GsonCollection loader for class:\t" + clazz);
	}

	private static final Map<String, GsonCollections<?>> CACHED_CLASSES = new HashMap<>();
 	private static final List<GsonCollections<?>> loaders = new ArrayList<>();
}
