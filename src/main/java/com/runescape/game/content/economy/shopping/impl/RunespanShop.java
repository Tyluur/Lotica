package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/26/2015
 */
public class RunespanShop extends AbstractGameShop {

	@Override
	public String currencyName() {
		return "Runespan Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getRunespanPoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setRunespanPoints(getCurrencyAmount(player) - amount);
	}

	@Override
	public Integer getBuyPrice(Item item) {
		int[] info = getItemInformation(item);
		if (info == null) {
			return Integer.MAX_VALUE;
		}
		return info[info.length - 1];
	}

	@Override
	public Integer getSellPrice(Item item) {
		int[] info = getItemInformation(item);
		if (info == null) {
			return Integer.MAX_VALUE;
		}
		return info[info.length - 1];
	}

	/**
	 * Finds item information from the {@link #ITEMS} array
	 *
	 * @param item
	 * 		The item
	 */
	private int[] getItemInformation(Item item) {
		for (int[] ITEM : ITEMS) {
			for (int aITEM : ITEM) {
				if (aITEM == item.getId()) {
					return ITEM;
				}
			}
		}
		return null;
	}

	/**
	 * An array of the item and prices in this shop
	 */
	private static final int[][] ITEMS = { { 6916, 750 },
	                                       { 6918, 750 },
	                                       { 6920, 750 },
	                                       { 6922, 750 },
	                                       { 6924, 750 },
	                                       { 6914, 1500 },
	                                       { 6889, 1500 },
	                                       { 13630, 500 },
	                                       { 5509, 100 },
	                                       { 5510, 175 },
	                                       { 5512, 225 },
	                                       { 5514, 400 },
	                                       { 13615, 1000 },
	                                       { 13614, 1000 },
	                                       { 13617, 1000 }
	};
}
