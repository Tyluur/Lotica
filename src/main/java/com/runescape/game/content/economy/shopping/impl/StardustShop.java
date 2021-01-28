package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.content.economy.shopping.AbstractGameShop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/29/2016
 */
public class StardustShop extends AbstractGameShop {

	public static final int CURRENCY = 13727;

	@Override
	public String currencyName() {
		return "Stardust";
	}

	@Override
	public Integer getCurrencyAmount(Player player) {
		return player.getInventory().getNumerOf(CURRENCY);
	}

	@Override
	public void deductCurrency(Player player, int amount) {
		player.getInventory().deleteItem(CURRENCY, amount);
	}

	@Override
	public Integer getBuyPrice(Item item) {
		switch (item.getId()) {
			case 1055:
				return 100_000;
			case 10400:
			case 10402:
			case 10404:
			case 10406:
			case 10408:
			case 10410:
			case 10412:
			case 10414:
				return 12_000;
			case 13661: // adze
				return 15_000;
			case 15259: // d pick
			case 6739: // d axe
				return 10_000;
			case 4447:
				return 7_500;
			default:
				return getDefaultBuyPrice(item);
		}
	}

	@Override
	public Integer getSellPrice(Item item) {
		return getBuyPrice(item);
	}
}
