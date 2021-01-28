package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Magic_Tree_Hater extends AbstractAchievement {

	@Override
	public String description() {
		return "Chop down @TOTAL@ magic trees.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 775_000));
	}

	@Override
	public Integer goal() {
		return 125;
	}
}
