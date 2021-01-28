package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Shark_Catcher extends AbstractAchievement {

	@Override
	public String description() {
		return "Catch @TOTAL@ sharks";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(12861));
	}

	@Override
	public Integer goal() {
		return 500;
	}
}
