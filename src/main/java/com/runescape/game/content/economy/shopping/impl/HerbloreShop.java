package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/8/2015
 */
public class HerbloreShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 199:
			case 201:
			case 203:
			case 205:
			case 209:
			case 211:
			case 213:
				return 2_500;
			case 207:
				return 10_000;
			case 3051:
			case 3049:
			case 215:
			case 217:
			case 219:
				return 3_250;
			case 2485:
				return 7_500;

			default:
				return getDefaultBuyPrice(item);
		}
	}
}
