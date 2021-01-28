package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Feather_Weight extends AbstractAchievement {

	@Override
	public String description() {
		return "Complete the gnome agility course @TOTAL@ times.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(88));
	}

	@Override
	public Integer goal() {
		return 25;
	}
}
