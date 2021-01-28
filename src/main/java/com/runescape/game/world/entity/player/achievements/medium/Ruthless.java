package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Ruthless extends AbstractAchievement {

	@Override
	public String description() {
		return "Kill " + goal() + " players.";
	}

	@Override
	public Integer goal() {
		return 15;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 1_000_000));
	}

	@Override
	public int interfaceItemId() {
		return 4151;
	}

}
