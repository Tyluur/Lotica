package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.cache.Cache;
import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.content.economy.shopping.StoreInstance;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.workers.game.core.CoresManager;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 30, 2015
 */
public class StoreLoader extends GsonCollections<StoreInstance> {

	public static void main(String[] args) throws IOException {
		Cache.init();
		CoresManager.init();
		GsonStartup.loadAll();
		List<String> lines = Utils.getFileText("C:\\Users\\Tyler\\Desktop\\Me\\Programming\\- SERVERS -\\600+\\Alotic\\world_server\\data\\items\\credit_shop.txt");
		StoreLoader loader = GsonStartup.getClass(StoreLoader.class);
		List<StoreInstance> localStoreList = loader.generateList();
		String category = null;
		List<Item> stock = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("#")) {
				continue;
			}

			System.out.println(line);

			if (line.contains("cat:")) {
				String shopSection = line.split("cat:")[1].trim();
				if (category != null) {
					category = "Gold - " + category;
					StoreInstance store = new StoreInstance(category);
					store.getStock().addAll(stock);
					localStoreList.add(store);
					stock.clear();
					System.out.println("added " + category + " store at this line");
				}
				category = shopSection;
			} else {
				String[] data = line.split(":");
				int itemId = Integer.parseInt(data[0]);
				stock.add(new Item(itemId, 1));
				System.out.println("Added item at this line");
			}
		}
		loader.save(localStoreList);
		System.exit(-1);
	}

	@Override
	public void initialize() {
		List<StoreInstance> localStoreList = generateList();
		RARE_STOCK_ITEMS.clear();
		for (StoreInstance store : localStoreList) {
			if (store == null) {
				continue;
			}
			if (hasRareStock(store)) {
				addStockToList(store);
			}
			stores.put(store.getName(), store);
			gameStores.clear();
		}
	}

	@Override
	public List<StoreInstance> generateList() {
		List<StoreInstance> list = super.generateList();
		Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return list;
	}

	@Override
	public String getFileLocation() {
		return "./data/resource/items/stores.json";
	}

	@Override
	public List<StoreInstance> loadList() {
		return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<StoreInstance>>() {}.getType());
	}

	/**
	 * Opens the shop for the player
	 *
	 * @param player
	 * 		The player to open the shop for
	 * @param name
	 * 		The name of the shop
	 * @return {@code true} if it was opened successfully
	 */
	public boolean openStore(Player player, String name) {
		StoreInstance store = getStore(name);
		if (store == null) {
			System.err.println("Unable to find shop by name " + name + "!");
			return false;
		}
		store.viewStore(player);
		return true;
	}

	/**
	 * Gets the shop that has a name matching the key
	 *
	 * @param key
	 * 		The key to search for
	 */
	public StoreInstance getStore(String key) {
		for (Entry<String, StoreInstance> entry : stores.entrySet()) {
			if (entry.getValue().getName().equalsIgnoreCase(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private boolean hasRareStock(StoreInstance store) {
		for (String name : invalidShopNames) {
			if (name.equalsIgnoreCase(store.getName())) {
				return true;
			}
		}
		return false;
	}

	private void addStockToList(StoreInstance store) {
		store.getStock().stream().filter(item -> !RARE_STOCK_ITEMS.contains(item.getId())).forEach(item -> RARE_STOCK_ITEMS.add(item.getId()));
	}

	public static boolean isContainedInRareStock(int itemId) {
		return RARE_STOCK_ITEMS.contains(itemId);
	}

	private static final String[] invalidShopNames = { "Wilderness Point Store", "PvP Armour Store", "Gold Point Rares", "Gold Point Weapons", "Gold Point Armour", "Gold Point Supplies", "Vote Store", "Jossik's Book Shop", "Monkey Madness Rewards", "Achievement Rewards", "Achievement Diary Equipment", "Runespan Point Exchange", "Wilderness Point Store 2", "Desert Treasure Shop", "Lunar Supplies" };

	private static final List<Integer> RARE_STOCK_ITEMS = new ArrayList<>();

	/**
	 * The map of the stores
	 */
	private static final Map<String, StoreInstance> stores = new HashMap<>();

	/**
	 * The map of game stores
	 */
	private static final Map<String, AbstractGameShop> gameStores = new HashMap<>();

	/**
	 * @param name
	 * 		The name of the shop we're getting the {@code AbstractGameShop} of
	 */
	public static AbstractGameShop getGameStore(String name) {
		StoreInstance store = stores.get(name);
		if (store == null) {
			return null;
		}
		AbstractGameShop gameStore = gameStores.get(name);
		if (gameStore != null) {
			return gameStore;
		}
		String currencyName = store.getCurrencyName();
		try {
			gameStores.put(name, gameStore = (AbstractGameShop) Class.forName(AbstractGameShop.class.getPackage().getName() + ".impl." + currencyName).newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return gameStore;
	}
}
