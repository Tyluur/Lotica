package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/30/2015
 */
public class VotePoints extends AbstractGameShop {
	
	@Override
	public String currencyName() {
		return "Vote Points";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getFacade().getVotePoints();
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getFacade().setVotePoints(getCurrencyAmount(player) - amount);
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
			return 1_000_000;
		} else if (itemId == 15273) {
			return 50;
		}
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
	private static final int[][] ITEMS = new int[][] {
			{ 995, 5 },
			{ 18768, 20 },
			{ 15273, 100 },
			{ 6737, 50 },
			{ 2572, 100 },
			{ 6570, 40 },
			{ 23659, 100 },
			{ 15332, 20 },
			{ 6585, 50 },
			{ 10551, 200 },
			{ 10548, 200 },
			{ 8850, 120 },
			{ 20072, 35 },
			{ 18337, 45 },
			{ 17273, 60 },
			{ 15486, 30 },
			{ 775, 20 },
			{ 22207, 22209, 22211, 22213, 40 }, // staff of lights
			{ 15441, 15442, 15443, 15444, 45 }, // whips
			{ 15701, 15702, 15703, 15704, 40 }, // dark bows
			{ 14525, 11926, 11928, 11930, 100 }, // sets
			{ 13663, 300 },
			// 2xp lamp
			{ 4447, 75 },
			// dyes
			{ 1763, 1765, 1767, 1771, 22347, 50 },
			// magic unguent
			{ 14061, 45 }, { 4151, 60 },
			// gold hammer
			{ 20084, 100 },
			// basket of eggs + sled
			{ 4565, 4084, 250 },
			// dogs and cats
			{ 12512, 12514, 12516, 12518, 12520, 12522, 100 },
			{ 1562, 1563, 1564, 1565, 1566, 7582, 100 }
	};

}
