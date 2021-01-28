package com.runescape.game.world.entity.player.achievements.easy;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Novice_Summoner extends AbstractAchievement {

	@Override
	public String description() {
		return "Make " + goal() + " wolf pouches.";
	}

	@Override
	public Integer goal() {
		return 30;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 100_000));
	}

	@Override
	public int interfaceItemId() {
		return 12047;
	}

}
