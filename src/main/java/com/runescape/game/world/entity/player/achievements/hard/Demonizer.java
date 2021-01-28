package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/24/2016
 */
public class Demonizer extends AbstractAchievement {

	@Override
	public String description() {
		return "Kill @TOTAL@ abyssal demons.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 500_000));
	}

	@Override
	public Integer goal() {
		return 500;
	}
}
