package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.interaction.controllers.impl.Barrows;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Spoils_of_Barrows extends AbstractAchievement {

	@Override
	public String description() {
		return "Loot @TOTAL@ barrows chests";
	}

	@Override
	public void reward(Player player) {
		addItem(player, Utils.randomArraySlot(Barrows.BARROW_REWARDS));
	}

	@Override
	public Integer goal() {
		return 50;
	}
}
