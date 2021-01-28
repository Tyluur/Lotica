package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/21/2015
 */
public class MeleeShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 4585:
			case 4087:
				return 600_000;
			case 7461:
				return 100_000;
			case 7462:
				return 200_000;
			case 3140:
				return 1_000_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}

}