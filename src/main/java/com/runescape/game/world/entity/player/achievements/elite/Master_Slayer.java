package com.runescape.game.world.entity.player.achievements.elite;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.hard.Hard_Slayer;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Master_Slayer extends AbstractAchievement {

	@Override
	public String description() {
		return "Complete @TOTAL@ elite slayer tasks";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(19888));
	}

	@Override
	public Integer goal() {
		return 40;
	}

	@Override
	public Class<?>[] requiredToComplete() {
		return new Class<?>[] { Hard_Slayer.class };
	}
}
