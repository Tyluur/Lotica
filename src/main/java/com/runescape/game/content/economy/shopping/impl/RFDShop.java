package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class RFDShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 7460:
				return 25_000;
			case 7459:
				return 10_000;
			case 7458:
				return 7_500;
			default:
				return getDefaultBuyPrice(item);
		}
	}
}