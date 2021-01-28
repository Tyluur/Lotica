package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class DefaultShop extends AbstractGameShop {

	@Override
	public String currencyName() {
		return "Coins";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getContainerCoins();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
	}

	@Override
	public Integer getBuyPrice(Item item) {
		return getDefaultBuyPrice(item);
	}

	@Override
	public Integer getSellPrice(Item item) {
		return getDefaultSellPrice(item);
	}

	@Override
	public boolean canBuyItem(Player player, Item item) {
		return true;
	}

	@Override
	public void purchaseItem(Player player, Item item, int amount) {
		if (!canBuyItem(player, item)) {
			return;
		}
		// the most amount of coins the player has
		int amountCoins = player.getContainerCoins();
		// the price of 1 of the item
		int price = getBuyPrice(item);
		// the max amount we can get
		int maxQuantity = amountCoins / price;
		// the amount we are purchasing
		int buyQuantity = amount;
		// if we can afford this
		boolean enoughCoins = maxQuantity >= buyQuantity;

	//	System.out.println("[amountCoins=" + amountCoins + ", price=" + price + ", maxQuantity=" + maxQuantity + ", buyQuantity=" + buyQuantity + "]");

		if (!enoughCoins) {
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
			if (player.takeMoney(totalPrice)) {
				player.getInventory().addItem(item.getId(), buyQuantity);
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("shop", player.getUsername(), "Purchased " + item + " for " + totalPrice + " " + currencyName() + "!"));
			}
		}
	}
}
