package com.runescape.utility.external.gson.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.resource.PlayerShopData.PlayerShop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class PlayerShopLoader {

	/**
	 * Gets the shop by the owner's name
	 *
	 * @param name
	 * 		The name of the shop owner
	 */
	public static PlayerShop getShop(String name) {
		synchronized (LOCK) {
			name = Utils.formatPlayerNameForProtocol(name);
			PlayerShop shop = SHOPS.get(name);
			if (shop != null) {
				return shop;
			}
			shop = GSON.fromJson(Utils.getText(getShopFile(name).getAbsolutePath()), PlayerShop.class);
			SHOPS.put(name, shop);
			return shop;
		}
	}

	/**
	 * Gets all game shops in existence
	 *
	 * @return A {@code List<PlayerShop>} object
	 */
	public static List<PlayerShop> getAllShops() {
		List<PlayerShop> shops = new ArrayList<>();
		for (File file : SHOP_DIRECTORY.listFiles()) {
			PlayerShop shop = getShop(file.getName().replaceAll(".json", ""));
			shops.add(shop);
		}
		return shops;
	}

	/**
	 * Creates a new shop for the player and saves it to the file
	 *
	 * @param player
	 * 		The player
	 */
	public static void createShop(Player player) {
		synchronized (LOCK) {
			PlayerShop shop = new PlayerShop(player.getUsername());
			save(shop);
		}
	}

	/**
	 * If there is a shop for the player
	 *
	 * @param username
	 * 		The name of the shop owner
	 */
	public static boolean hasShop(String username) {
		synchronized (LOCK) {
			return getShopFile(Utils.formatPlayerNameForProtocol(username)).exists();
		}
	}

	/**
	 * Deletes a players shop from the system
	 *
	 * @param username
	 * 		The name of the player
	 */
	public static boolean deleteShop(String username) {
		if (hasShop(username)) {
			File shopFile = getShopFile(username);
			boolean deleted = shopFile.delete();
			if (deleted) {
				PlayerShop removed = SHOPS.remove(username);
				if (removed == null) {
					System.out.println("Deleted shop from file but it didnt exist in map.");
				} else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Constructs a {@code File} object of the shop file. This is used for reading from the file, and checking for its
	 * existance.
	 *
	 * @param ownerName
	 * 		The name of the shop owner
	 */
	protected static File getShopFile(String ownerName) {
		return new File(SHOP_DIRECTORY.getAbsolutePath() + "/" + ownerName + ".json");
	}

	/**
	 * Saves the shop to a file
	 *
	 * @param shop
	 * 		The shop to save
	 */
	public static void save(PlayerShop shop) {
		synchronized (LOCK) {
			try (Writer writer = new FileWriter(getShopFile(shop.getOwnerName()))) {
				GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
				Gson gson = builder.create();
				gson.toJson(shop, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The map of shops
	 */
	private static final Map<String, PlayerShop> SHOPS = new HashMap<>();

	/**
	 * The gson instance
	 */
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The object which all shop actions are synchronized through
	 */
	public static final Object LOCK = new Object();

	/**
	 * The directory of the player owned shops
	 */
	private static final File SHOP_DIRECTORY = new File(GameConstants.FILES_PATH + "players/shops/");
}
