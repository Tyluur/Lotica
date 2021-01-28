package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Matchmaker extends AbstractAchievement {

	@Override
	public String description() {
		return "Light @TOTAL@ magic logs.";
	}

	@Override
	public Integer goal() {
		return 200;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 100_000));
	}

	@Override
	public int interfaceItemId() {
		return 1513;
	}

}
