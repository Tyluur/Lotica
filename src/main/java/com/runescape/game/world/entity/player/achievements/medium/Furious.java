package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Furious extends AbstractAchievement {

	@Override
	public String description() {
		return "Equip an amulet of fury";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 150_000));
	}

	@Override
	public Integer goal() {
		return 1;
	}
}
