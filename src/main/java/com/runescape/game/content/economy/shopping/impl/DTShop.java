package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/21/2015
 */
public class DTShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 22207:
			case 22209:
			case 22211:
			case 22213:
				return 1_000_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}

}