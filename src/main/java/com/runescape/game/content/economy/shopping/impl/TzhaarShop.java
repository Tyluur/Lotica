package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class TzhaarShop extends AbstractGameShop {

	public static final int TOKKUL_ID = 6529;

	@Override
	public String currencyName() {
		return "Tokkul";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getInventory().getNumerOf(TOKKUL_ID);
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getInventory().deleteItem(TOKKUL_ID, amount);
	}

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 6522:
				return 375;
			case 6523:
				return 60_000;
			case 6524:
				return 67_500;
			case 6525:
				return 37_500;
			case 6526:
				return 52_500;
			case 6527:
				return 45_000;
			case 6528:
				return 75_000;
			case 6568:
				return 90_000;
			case 6571:
				return 120_000;
			default:
				return getDefaultBuyPrice(item);
		}
	}

	@Override
	public Integer getSellPrice(Item item) {
		return getBuyPrice(item);
	}

}
