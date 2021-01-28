package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/24/2016
 */
public class I_Got_The_Blues extends AbstractAchievement {

	@Override
	public String description() {
		return "Kill @TOTAL@ blue dragons.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 250_000), new Item(537, 100));
	}

	@Override
	public Integer goal() {
		return 250;
	}
}
