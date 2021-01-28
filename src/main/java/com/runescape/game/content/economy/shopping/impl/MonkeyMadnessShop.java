package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class MonkeyMadnessShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 19784: // korasi
				return 50_000_000;
			case 4031: // greegree monkey
				return 3_000_000;
			case 4029: // greegree zombie
				return 4_000_000;
			case 4024: // greegree ninja
				return 5_000_000;
			case 4026:
				return 10_000_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}

	
}
