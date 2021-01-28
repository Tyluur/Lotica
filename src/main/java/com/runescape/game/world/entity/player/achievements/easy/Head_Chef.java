package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Head_Chef extends AbstractAchievement {

	@Override
	public String description() {
		return "Cook @TOTAL@ food.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(775));
	}

	@Override
	public Integer goal() {
		return 1_000;
	}
}
