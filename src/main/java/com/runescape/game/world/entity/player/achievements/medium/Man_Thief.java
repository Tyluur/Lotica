package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Man_Thief extends AbstractAchievement {

	@Override
	public String description() {
		return "Pickpocket " + goal() + " men.";
	}

	@Override
	public Integer goal() {
		return 75;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 300_000));
	}

	@Override
	public int interfaceItemId() {
		return 2631;
	}

}
