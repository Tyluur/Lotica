package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Journey_Man_Slayer extends AbstractAchievement {

	@Override
	public String description() {
		return "Complete @TOTAL@ medium slayer tasks.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(18337));
	}

	@Override
	public Integer goal() {
		return 10;
	}

	@Override
	public Class<?>[] requiredToComplete() {
		return new Class[] { Slayer_Hunter.class };
	}
}
