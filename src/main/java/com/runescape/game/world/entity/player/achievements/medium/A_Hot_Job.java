package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class A_Hot_Job extends AbstractAchievement {

	@Override
	public String description() {
		return "Smelt @TOTAL@ gold bars";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(776));
	}

	@Override
	public Integer goal() {
		return 2000;
	}
}
