package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Novice_Clue_Finisher extends AbstractAchievement {

	@Override
	public String description() {
		return "Finish @TOTAL@ easy clues";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 500_000));
	}

	@Override
	public Integer goal() {
		return 5;
	}
}
