package com.runescape.game.content.economy.shopping;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class StoreInstance {

	/**
	 * The id of the store interface
	 */
	public static final int STORE_INTERFACE_ID = 620;

	/**
	 * The id of the store inventory interface
	 */
	public static final int STORE_INVENTORY_ID = 207;

	/**
	 * The name of the store
	 */
	private String name;

	/**
	 * If players can sell back to the store
	 */
	private boolean canSellBack;

	/**
	 * The name of the currency class used in this store
	 */
	private String currencyName;

	/**
	 * The items in the store
	 */
	private List<Item> stock = new ArrayList<>();

	/**
	 * The instance of the game shop for the store
	 */
	private transient AbstractGameShop gameShop;

	/**
	 * The list of players viewing the store
	 */
	private transient List<Player> viewers;

	/**
	 * Constructs a new store
	 *
	 * @param name
	 * 		The name of the store
	 */
	public StoreInstance(String name) {
		this.name = name;
		this.canSellBack = false;
		this.currencyName = "DefaultShop";
	}

	/**
	 * Shows the player the store
	 *
	 * @param player
	 * 		The player
	 */
	public void viewStore(Player player) {
		player.stopAll();

		player.getAttributes().put("viewing_store", this);

		sendStoreInterface(player);
		sendInventoryInterface(player);
		getViewers().add(player);
		refreshStock(player);

		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("shop", player.getUsername(), "Opened store " + getName() + "."));

		player.setCloseInterfacesEvent(() -> {
			player.getAttributes().remove("viewing_store");
			getViewers().remove(player);
		});
	}

	/**
	 * This method sends the store interface configurations to the player
	 *
	 * @param player
	 * 		The player
	 */
	private void sendStoreInterface(Player player) {
		player.getPackets().sendConfig(118, 4);
		player.getPackets().sendConfig(1496, -1); // sets samples items set
		if (getGameShop() == null) {
			throw new IllegalStateException("AbstractGameShop instance for shop " + getName() + " was not found.");
		}
		player.getPackets().sendConfig(532, getGameShop().currencyName().contains("Tokkul") ? 6529 : 995);
		player.getPackets().sendGlobalConfig(199, -1);// unknown
		player.getPackets().sendGlobalConfig(1241, 16750848);// unknown
		player.getPackets().sendGlobalConfig(1242, 15439903);// unknown
		player.getPackets().sendGlobalConfig(741, -1);// unknown
		player.getPackets().sendGlobalConfig(743, -1);// unknown
		player.getPackets().sendGlobalConfig(744, 0);// unknown
		player.getPackets().sendIComponentSettings(620, 25, 0, stock.size() * 6, 1150); // unlocks stock slots
		player.getPackets().sendIComponentText(620, 20, getName());
		refreshCurrencyAmount(player);
		player.getInterfaceManager().sendInterface(620); // opens shop
	}

	/**
	 * This method sends the inventory configurations to the player
	 *
	 * @param player
	 * 		The player
	 */
	private void sendInventoryInterface(Player player) {
		player.getPackets().sendUnlockIComponentOptionSlots(STORE_INVENTORY_ID, 0, 0, 27, 0, 1, 2, 3, 4, 5, 6);
		player.getPackets().sendInterSetItemsOptionsScript(STORE_INVENTORY_ID, 0, 93, 4, 7, "Value", "Sell-1", "Sell-5", "Sell-10", "Sell-All", "Sell-X", "Examine");
		player.getInterfaceManager().sendInventoryInterface(STORE_INVENTORY_ID);
	}

	/**
	 * Refresh the currency amount
	 *
	 * @param player
	 * 		The player
	 */
	public void refreshCurrencyAmount(Player player) {
		player.getPackets().sendIComponentText(620, 24, "You currently have " + Utils.format(getGameShop().getCurrencyAmount(player)) + " " + getGameShop().currencyName().toLowerCase() + ".");
	}

	/**
	 * This method refreshes the stock for everyone viewing the store
	 */
	private void refreshAllViewing() {
		viewers.forEach(this::refreshStock);
	}

	/**
	 * This method refreshes the stock of items to the viewer
	 *
	 * @param viewer
	 * 		The viewer
	 */
	public void refreshStock(Player viewer) {
		viewer.getPackets().sendItems(4, stock.toArray(new Item[stock.size()]));
	}

	/**
	 * Tells the player the value of the item in the slot
	 *
	 * @param player
	 * 		The player
	 * @param slotId
	 * 		The slot clicked
	 * @param inventoryItem
	 * 		If we are checking an item in the inventory or an item in the store
	 */
	public void sendValue(Player player, int slotId, boolean inventoryItem) {
		Item item = inventoryItem ? player.getInventory().getItem(slotId) : stock.get(slotId / 6);
		if (item == null) {
			return;
		}
		if (player.getAttribute("debugging_shop_items") != null) {
			boolean append = player.getAttribute("debugging_shop_items");
			player.sendMessage(item.toString());
			try {
				String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

				StringSelection stringSelection = new StringSelection(append ? ("" + (data.trim().length() > 1 ? data + ", " : "") + item.getId()) : "" + item.getId());
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
		if (inventoryItem) {
			if (!canSellBack) {
				player.sendMessage("You can't sell items to this store.");
				return;
			}
			getGameShop().sendDefaultItemPricing(player, item, true);
		} else {
			AbstractGameShop shop = getGameShop();
			if (shop == null) {
				return;
			}
			shop.sendDefaultItemPricing(player, item, false);
		}
	}

	/**
	 * Handles the buying of items from the stock
	 *
	 * @param player
	 * 		The player
	 * @param slotId
	 * 		The slotId of the item to buy
	 * @param amount
	 * 		The amount of the item to buy
	 */
	public void buyItem(Player player, int slotId, int amount) {
		Item item = stock.get(slotId / 6);
		if (item == null) {
			return;
		}
		if (player.isAnyIronman() && !canIronmanBuyFrom(name)) {
			player.sendMessage("As an ironman account, you can't purchase any items from this store.");
			return;
		}
		if (item.getAmount() == 0) {
			player.getPackets().sendGameMessage("There is no stock of that item at the moment.");
			return;
		}
		if (getGameShop() != null) {
			getGameShop().purchaseItem(player, item, amount);
		}
		refreshCurrencyAmount(player);
	}

	/**
	 * This method sells the item to the store
	 *
	 * @param player
	 * 		The player
	 * @param slotId
	 * 		The slot id of the item being sold
	 * @param amount
	 * 		The amount of the item to sell
	 */
	public void sellItem(Player player, int slotId, int amount) {
		if (!canSellBack) {
			player.sendMessage("You can't sell to this store.");
			return;
		}
		Item item = player.getInventory().getItem(slotId);
		if (item == null) {
			return;
		}
		int originalId = item.getId();
		if (item.getDefinitions().isNoted()) {
			item = new Item(item.getDefinitions().getCertId(), item.getAmount());
		}
		if (item.getDefinitions().isDestroyItem() || !ItemConstants.isTradeable(item) || item.getId() == 995) {
			player.getPackets().sendGameMessage("You can't sell this item.");
			return;
		}
		if (getGameShop() == null) {
			return;
		}
		if (!canSellToStore(item)) {
			player.sendMessage("You can't sell this item to the store.");
			return;
		}
		int price = gameShop.getDefaultSellPrice(item);
		int numberOff = player.getInventory().getItems().getNumberOf(originalId);
		if (amount > numberOff) {
			amount = numberOff;
		}
		int cash = price * amount;
		player.getInventory().deleteItem(originalId, amount);
		player.getInventory().addItem(995, cash);
		refreshCurrencyAmount(player);
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("shop", player.getUsername(), "Sold " + item + " for " + cash + " " + getGameShop().currencyName() + "!"));
	}

	private boolean canSellToStore(Item item) {
		if (isGeneralStore()) {
			return true;
		}
		for (Item sItem : stock) {
			if (sItem.getId() == item.getId()) {
				return true;
			}
		}
		return false;
	}

	private boolean isGeneralStore() {
		return getName().toLowerCase().contains("general store");
	}

	/**
	 * This method gets the viewers
	 */
	public List<Player> getViewers() {
		if (viewers == null) {
			viewers = new ArrayList<>();
		}
		return viewers;
	}

	/**
	 * Gets the name of the store
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the store
	 *
	 * @param name
	 * 		The name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the stock
	 */
	public List<Item> getStock() {
		return stock;
	}

	/**
	 * Sets the stock
	 *
	 * @param stock
	 * 		The stock to set
	 */
	public void setStock(List<Item> stock) {
		this.stock = stock;
	}

	/**
	 * If we can sell back
	 */
	public boolean canSellBack() {
		return canSellBack;
	}

	/**
	 * Sets if we can sell back to the store
	 *
	 * @param canSellBack
	 * 		The flag to set
	 */
	public void setCanSellBack(boolean canSellBack) {
		this.canSellBack = canSellBack;
	}

	/**
	 * Finds an item from the {@link #stock}  by its id
	 *
	 * @param itemId
	 * 		The id of the item to find
	 * @return A {@code Item}
	 */
	public Item getItem(int itemId) {
		for (Item item : stock) {
			if (item.getId() == itemId) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Gets an item in a slot
	 *
	 * @param slotId
	 * 		The slot
	 */
	public Item getItemInSlot(int slotId) {
		return stock.get(slotId);
	}

	/**
	 * Finds the game shop for our store
	 */
	private AbstractGameShop getGameShop() {
		if (gameShop == null) {
			gameShop = StoreLoader.getGameStore(getName());
		}
		return gameShop;
	}

	/**
	 * Adding an item to the {@link #stock} items
	 *
	 * @param item
	 * 		The item to add
	 */
	public void addItemToStock(Item item) {
		System.out.println("aDDED Item:\t" + item);
		stock.add(item);
	}

	public boolean canIronmanBuyFrom(String shopName) {
		switch (shopName) {
			case "Edgeville General Store":
			case "Ironman Shop":
			case "Wilderness Point Store":
			case "Wilderness Point Store 2":
			case "Wilderness Supplies":
			case "Brawling Gloves Store":
			case "PvP Armour Store":
			case "Gold Point Rares":
			case "Gold Point Weapons":
			case "Gold Point Armour":
			case "Gold Point Supplies":
			case "Gold - Featured":
			case "Gold - Armory":
			case "Gold - Miscellaneous":
			case "Gold - Miscellaneous 2":
			case "Gold - Weaponry":
			case "Vote Store":
			case "Advanced Summoning Store":
			case "Basic Summoning Store":
			case "Jossik's Book Shop":
			case "Monkey Madness Rewards":
			case "Achievement Rewards":
			case "Achievement Diary Equipment":
			case "Runespan Point Exchange":
			case "Desert Treasure Shop":
			case "Slayer Point Store":
			case "Culinaromancer Items":
			case "Culinaromancer Food":
			case "Lunar Supplies":
			case "Nightmare Zone Shop":
			case "Stardust Exchange":
				return true;
			default:
				return false;
		}
	}

    public String getCurrencyName() {
        return this.currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }
}