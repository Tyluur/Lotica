package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/28/2015
 */
public class GoldPoints extends AbstractGameShop {

	public static final int GOLD_POINT_TICKET = 13663;
	
	/**
	 * An array of the item and prices in this shop
	 */
	private static final int[][] ITEMS = new int[][] {
			// featured
			{ GOLD_POINT_TICKET, 100 }, { 18337, 499}, { 6199, 499}, { 990, 499 }, { 14061, 799 }, { 4447, 699 }, { 1763, 1765, 1767, 1771, 22347, 500 }, { 15263, 199 }, { 537, 399 }, { 1437, 199 }, { 7937, 199 }, { 12160, 750 }, { 12163, 1000 }, { 12159, 500 }, { 12158, 250 }, { 6570, 999 }, { 18344, 18839, 499 }, { 19669, 999 }, { 18335, 999 }, { 13847, 13846, 13848, 13845, 13850, 13851, 13855, 13853, 13849, 13857, 13852, 13854, 13856, 1000 },
			// weaponry
			{ 19784, 999 }, { 18349, 18351, 18353, 1099 }, { 13899, 899 }, { 13923, 599 }, { 13905, 799 }, { 13929, 499 }, { 13926, 599 }, { 13902, 799 }, { 18355, 699 }, { 13867, 699 }, { 13941, 399 }, { 18357, 999 }, { 13883, 13879, 199 }, { 8841, 599 },
			// armory
			{ 10548, 500 }, { 10551, 1299 }, { 8849, 500 }, { 8850, 750 }, { 20072, 999 }, { 18359, 18363, 18361, 1499 }, { 13920, 599 }, { 13896, 1099 }, { 13884, 1099 }, { 13908, 599 }, { 1099, 2500 }, { 13890, 999 },  {13914, 599}, { 13887, 1099 }, { 13911, 599 }, { 13893, 1099 }, { 13917, 599}, { 13864, 799 }, { 13938, 399},  { 13858, 799 }, { 13932, 399 }, { 13861, 799 }, { 13935, 399 }, { 13876, 599 }, { 13950, 399 }, { 13870, 799 }, { 13944, 499 }, { 13873, 699 }, { 13947, 399 }, { 11665, 11663, 11664, 599 }, { 8840, 899 }, { 8842, 500 }, { 8839, 799 },
			// misc
			{ 18744, 18745, 18746, 499 }, { 1506, 500 }, { 7003, 500 }, { 4502, 500 }, { 6665, 1000 }, { 6666, 1000 }, { 19708, 19706, 19707, 399 }, { 20178, 20177, 399 }, { 18667, 199 }, { 20727, 499 }, { 20728, 499 }, { 15673, 299 }, { 14728, 499 }, { 1037, 1099 }, { 4566, 599 }, { 11021, 11020, 11022, 11019, 499 }, { 1419, 9920, 599 }, { 11789, 699 }, { 15352, 699 }, { 9925, 9924, 9923, 9922, 9921, 399 }, { 15426, 499 }, { 14595, 14603, 699 }, { 14605, 399 }, { 14602, 399 },
			// misc 2
			{ 15422, 15423, 15425, 299 }, { 20044, 20045, 20046, 399 }, { 10836, 10837, 10838, 399 }, { 10839, 10840, 299 }, { 13615, 13614, 13617, 499 }, { 13620, 13619, 13622, 499 }, { 13625, 13624, 13627, 499 }, { 22377, 5000 }, { 22385, 5000 }, { 22389, 5000 }, { 22379, 5000 }, { 22386, 5000 }, { 22390, 5000 }, { 22381, 5000 }, { 22387, 5000 }, { 22391, 5000 }, { 22383, 5000 }, { 22388, 5000 }, { 22392, 5000 }, { 14759, 14751, 14747, 14743, 14761, 14755, 14745, 14757, 14749, 14753, 14769, 14771, 14763, 14765, 14767, 14773, 14777, 14779, 14775, 14781, 14783, 14785, 14789, 14787, 14791, 199 }, { 19747, 750 }, };

	@Override
	public String currencyName() {
		return "Gold Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getGoldPoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setGoldPoints(getCurrencyAmount(player) - amount);
	}

	@Override
	public Integer getBuyPrice(Item item) {
		if (item.getId() == 3709) {
			return 0;
		}
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
		switch (itemId) {
			case 537:
				return 250;
			case 990:
				return 25;
			case 12160:
			case 12163:
			case 12159:
			case 12158:
				return 100;
			case GOLD_POINT_TICKET:
				return 100;
			case 13883:
			case 13879:
				return 50;
			case 1437:
				return 1000;
			case 7937:
				return 500;
			default:
				return 1;
		}
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
