package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Star_Lord extends AbstractAchievement {

	@Override
	public String description() {
		return "Be the first to tag a shooting star.";
	}

	@Override
	public void reward(Player player) {
		player.getSkills().addXpNoModifier(Skills.MINING, 100_000);
	}

	@Override
	public Integer goal() {
		return 1;
	}
}
