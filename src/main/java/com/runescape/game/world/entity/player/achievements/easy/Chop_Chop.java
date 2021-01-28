package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Chop_Chop extends AbstractAchievement {

	@Override
	public String description() {
		return "Cut " + goal() + " normal logs.";
	}

	@Override
	public Integer goal() {
		return 50;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 100_000));
	}

	@Override
	public int interfaceItemId() {
		return 6739;
	}

}
