package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Gold_Mine extends AbstractAchievement {

	@Override
	public String description() {
		return "Mine @TOTAL@ gold ore";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(20787), new Item(20788), new Item(20789), new Item(20790), new Item(20791), new Item(20792));
	}

	@Override
	public Integer goal() {
		return 1000;
	}
}
