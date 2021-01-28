package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/1/2015
 */
public class SlayerPoints extends AbstractGameShop {
	
	@Override
	public String currencyName() {
		return "Slayer Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getSlayerPoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setSlayerPoints(getCurrencyAmount(player) - amount);
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

	@Override
	protected int getItemAmount(int itemId) {
		return 1;
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
	private static final int[][] ITEMS = new int[][] { { 10551, 400 }, { 15492, 750 }, { 11967, 600 }, { 15488, 350 }, { 4153, 100 }, { 21369, 5_000 }, { 4170, 400 }, { 4155, 0 } };
}
