package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.medium.Shark_Catcher;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Sharksman extends AbstractAchievement {

	@Override
	public String description() {
		return "Fish @TOTAL@ sharks.";
	}

	@Override
	public Integer goal() {
		return 1000;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 250_000));
	}

	@Override
	public int interfaceItemId() {
		return 385;
	}

	@Override
	public Class<?>[] requiredToComplete() {
		return new Class[] { Shark_Catcher.class };
	}
}
