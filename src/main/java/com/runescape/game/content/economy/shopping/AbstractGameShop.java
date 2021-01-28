package com.runescape.game.content.economy.shopping;

import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public abstract class AbstractGameShop {

	/**
	 * The name of the currency this shop uses
	 */
	public abstract String currencyName();

	/**
	 * The amount of our currency the player has
	 *
	 * @param player
	 * 		The player
	 */
	public abstract Integer getCurrencyAmount(Player player);

	/**
	 * Deducts the currency from the player
	 *
	 * @param player
	 * 		The player
	 * @param amount
	 * 		The amount to deduct
	 */
	public abstract void deductCurrency(Player player, int amount);

	/**
	 * The price the item costs in the store
	 *
	 * @param item
	 * 		The item
	 */
	public abstract Integer getBuyPrice(Item item);

	/**
	 * The price the item will be sold for by the player
	 *
	 * @param item
	 * 		The item
	 */
	public abstract Integer getSellPrice(Item item);

	/**
	 * If the player can buy this item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 */
	public boolean canBuyItem(Player player, Item item) {
		return true;
	}

	/**
	 * Purchases the item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 * @param amount
	 * 		The amount of the item
	 */
	public void purchaseItem(Player player, Item item, int amount) {
		if (!canBuyItem(player, item)) {
			return;
		}
		// the most amount of currency the player has
		int amountCoins = getCurrencyAmount(player);

		// the price of 1 of the item
		int price = findBuyPrice(item);

		// the max amount we can get
		int maxQuantity;
		if (price > 0) {
			maxQuantity = amountCoins / price;
		} else {
			maxQuantity = item.getAmount();
		}

		// the amount we are purchasing
		int buyQuantity = amount;

		// if we can afford this
		boolean enoughCurrency = maxQuantity >= buyQuantity;

		//System.out.println("[amountCoins=" + amountCoins + ", price=" + price + ", maxQuantity=" + maxQuantity + ", buyQuantity=" + buyQuantity + "]");

		if (!enoughCurrency) {
			sendBrokeMessage(player, item, price);
			buyQuantity = maxQuantity;
		}
		if (item.getDefinitions().isStackable()) {
			if (player.getInventory().getFreeSlots() < 1) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
		} else {
			int freeSlots = player.getInventory().getFreeSlots();
			if (buyQuantity > freeSlots) {
				buyQuantity = freeSlots;
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
			}
		}
		if (buyQuantity != 0) {
			int totalPrice = price * buyQuantity;
			boolean canTakeMoney = getCurrencyAmount(player) >= totalPrice;
			if (canTakeMoney) {
				deductCurrency(player, totalPrice);
				player.getInventory().addItem(item = new Item(item.getId(), buyQuantity * getItemAmount(item.getId())));
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("shop", player.getUsername(), "Purchased " + item + " for " + totalPrice + " " + currencyName() + "!"));
			}
		}
	}

	/**
	 * On purchase, some items have different quantities that players should receive.
	 *
	 * @param itemId
	 * 		The id of the item
	 * @return The amount of the item the player should get per buy. Usually 1.
	 */
	protected int getItemAmount(int itemId) {
		return 1;
	}

	/**
	 * Gives the item to the player
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 */
	protected boolean giveItem(Player player, Item item) {
		return player.getInventory().addItem(item);
	}

	/**
	 * Gets the description of the shop. This is displayed below the items.
	 *
	 * @param player
	 * 		The player viewing the shop
	 */
	public String getDescription(Player player) {
		return currencyName() + ": " + Utils.format(getCurrencyAmount(player));
	}

	/**
	 * The default price the item will be bought for from the cache.
	 *
	 * @param item
	 * 		The item
	 */
	protected Integer getDefaultBuyPrice(Item item) {
		return Math.max(item.getDefinitions().getValue(), 1);
	}

	/**
	 * The default price the item will be sold for.
	 *
	 * @param item
	 * 		The item
	 */
	protected Integer getDefaultSellPrice(Item item) {
		int price = ClientScriptMap.getMap(1441).getIntValue(item.getId());
		if (price > 0) {
			return price;
		}
		return Math.max(1, (item.getDefinitions().getValue() * 30) / 100);
	}

	/**
	 * Sends the message to the player if they are too broke to buy the item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 * @param price
	 * 		The price of the item
	 */
	protected void sendBrokeMessage(Player player, Item item, int price) {
		player.sendMessage("You need " + Utils.format(price) + " " + currencyName().toLowerCase() + " to purchase this item.", false);
	}

	/**
	 * This method finds the price of an item from the {@link #getBuyPrice(Item)} method, and prints information about
	 * the item if it doesn't exists for easy additions
	 *
	 * @param item
	 * 		The item
	 */
	private int findBuyPrice(Item item) {
		int buyPrice = getBuyPrice(item);
		if (buyPrice >= Integer.MAX_VALUE) {
			System.out.println("No price set for item:\t" + item);
		}
		return buyPrice;
	}

	/**
	 * Sends a message about the pricing of the item to the player
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 * @param selling
	 * 		If we are selling the item
	 */
	public void sendDefaultItemPricing(Player player, Item item, boolean selling) {
		if (!selling) {
			int amount = getItemAmount(item.getId());
			int buyPrice = findBuyPrice(item);
			if (buyPrice == Integer.MAX_VALUE) {
				player.sendMessage("<col=" + ChatColors.RED + ">This item has no price. " + (player.hasPrivilegesOf(RightManager.OWNER) ? item : "Please report this on forums."));
			} else {
				player.sendMessage((amount == 1 ? "" : Utils.format(amount) + "x ") + item.getName() + ": shop will sell for <col=" + ChatColors.BLUE + ">" + (buyPrice == 0 ? "FREE" : Utils.format(buyPrice) + " " + currencyName().toLowerCase() + "") + "</col>.", false);
			}
		} else {
			player.sendMessage(item.getName() + " will sell for " + Utils.format(getDefaultSellPrice(item)) + " " + currencyName().toLowerCase() + ".", false);
		}
	}
}
