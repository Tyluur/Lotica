package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class WildernessPoints extends AbstractGameShop {

	@Override
	public String currencyName() {
		return "Wilderness Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getWildernessPoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setWildernessPoints(player.getFacade().getWildernessPoints() - amount);
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
		if (itemId == 995) {
			return 5_000_000;
		} else if (itemId == 21773) {
			return 3;
		} else {
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

	/**
	 * An array of the item and prices in this shop
	 */
	private static final int[][] ITEMS = new int[][] {
			// main store
			{ Wilderness.WILDERNESS_TOKEN, 1 }, { 15243, 1 }, { 3145, 1 }, { 21773, 1 }, { 21777, 15_000 }, { 10499, 300 }, { 15241, 1_500 }, { 18349, 18351, 18353, 18355, 18357, 27_500 }, { 18359, 27_500 }, { 18361, 25_500 }, { 18363, 27_500 }, { 18335, 3000 }, { 19784, 29_500 }, { 995, 1500 }, { 21371, 4000 }, { 10548, 1200 }, { 10551, 4250 }, { 10887, 1250 }, { 13845, 1000 }, { 13846, 1000 }, { 13847, 1000 }, { 13848, 1000 }, { 13849, 1200 }, { 13850, 950 }, { 13851, 950 }, { 13852, 950 }, { 13853, 950 }, { 13854, 950 }, { 13855, 950 }, { 13856, 950 }, { 13857, 950 }, { 22358, 22359, 22360, 22361, 1_250 }, { 22362, 22363, 22364, 22365, 1_000 }, { 22366, 22367, 22368, 22369, 1_000 }, { 4151, 750 },
			// pvp armour
			{ 13887, 13893, 3_000 }, { 13899, 13905, 13902, 2_500 }, { 13896, 13884, 13890, 3_000 }, { 13876, 13873, 13870, 2_000 }, { 13883, 12 }, { 13879, 10 }, { 13864, 13858, 13861, 13867, 1_750 },
			// misc
			{ 19669, 10_000 },
			// basic void
			{ 11665, 11664, 11663, 1_500 }, { 8839, 8840, 750 }, { 8842, 500 },
			// elite void
			{ 19785, 19786, 3_000 },
			// rings
			{ 15220, 3_000 }, { 15020, 15019, 15018, 2_500 }, { 11694, 14484, 5_000 }, { 20072, 1_000 }, { 2581, 2577, 2_000 },
			// infinity
			{ 6920, 6922, 2_500 },
			{ 6918, 6916, 6924, 3_000 },
			// master wand
			{ 6914, 3_500 },
			{ 6889, 5000 }

	};

}
