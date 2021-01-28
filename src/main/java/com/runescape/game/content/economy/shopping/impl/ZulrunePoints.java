package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class ZulrunePoints extends AbstractGameShop {

	/**
	 * An array of the item and prices in this shop
	 */
	private static final int[][] ITEMS = new int[][] { { 18839, 18344, 5_000}, { 12929, 4000}, { 12915, 3000}, { 8839, 8840, 8841, 8842, 7_500 }, { 11663, 11664, 11665, 12_500 }, { 19786, 19785, 10_500 }, { 18335, 35_000 }, { 23659, 35_000 }, { 18349, 18351, 18353, 125_000 }, { 18355, 18357, 75_000 }, { 18359, 18361, 18363, 65_000 }, { 19784, 35_000 } };

	@Override
	public String currencyName() {
		return "Dream Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getDreamPoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setDreamPoints(getCurrencyAmount(player) - amount);
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

}
