package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Novice_Runecrafter extends AbstractAchievement {

	@Override
	public String description() {
		return "Craft @TOTAL@ essence.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(5509));
	}

	@Override
	public Integer goal() {
		return 100;
	}
}
