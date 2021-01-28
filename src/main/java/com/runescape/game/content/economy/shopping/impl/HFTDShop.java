package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class HFTDShop extends DefaultShop {

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 3840:
			case 3842:
			case 3844:
				return 100_000;
			case 19613:
			case 19615:
			case 19617:
				return 1_250_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}
}
