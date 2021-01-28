package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Vine_Whippy extends AbstractAchievement {

	@Override
	public String description() {
		return "Create a vine whip once.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 350_000));
	}

	@Override
	public Integer goal() {
		return 1;
	}
}
