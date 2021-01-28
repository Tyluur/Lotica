package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.easy.Novice_Runecrafter;
import com.runescape.game.world.entity.player.achievements.medium.Embrace_The_Abyss;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Master_of_Creation extends AbstractAchievement {

	@Override
	public String description() {
		return "Craft @TOTAL@ essence";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(5514));
	}

	@Override
	public Integer goal() {
		return 1000;
	}

	@Override
	public Class<?>[] requiredToComplete() {
		return new Class<?>[] { Novice_Runecrafter.class, Embrace_The_Abyss.class };
	}
}
