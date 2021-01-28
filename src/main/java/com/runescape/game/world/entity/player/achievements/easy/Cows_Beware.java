package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Cows_Beware extends AbstractAchievement {

	@Override
	public String description() {
		return "Kill @TOTAL@ Cows";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 75_000));
	}

	@Override
	public Integer goal() {
		return 250;
	}
}
