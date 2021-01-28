package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Barrows_Addiction extends AbstractAchievement {

	@Override
	public String description() {
		return "Do @TOTAL@ barrows runs.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 750_000));
	}

	@Override
	public Integer goal() {
		return 15;
	}
}
