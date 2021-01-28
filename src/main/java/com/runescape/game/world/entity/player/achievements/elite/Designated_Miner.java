package com.runescape.game.world.entity.player.achievements.elite;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class Designated_Miner extends AbstractAchievement {
	
	@Override
	public String description() {
		return "Mine @TOTAL@ rune ore.";
	}

	@Override
	public Integer goal() {
		return 500;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(15259));
	}

	@Override
	public int interfaceItemId() {
		return 15259;
	}
}
