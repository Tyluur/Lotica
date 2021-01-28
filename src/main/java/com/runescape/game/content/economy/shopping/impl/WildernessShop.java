package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class WildernessShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 3840:
			case 3842:
			case 3844:
				return 7_500;
			case 6109:
			case 6107:
			case 6108:
			case 6106:
			case 6110:
			case 6111:
				return 5_000;
			case 2412:
			case 2413:
			case 2414:
				return 25_000;
			case 2415:
			case 2416:
			case 2417:
				return 50_000;
			case 10498:
			case 10499:
				return 5_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}
}
