package com.runescape.game.world.entity.player.achievements.medium;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.easy.Novice_Runecrafter;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/30/2016
 */
public class Embrace_The_Abyss extends AbstractAchievement {

	@Override
	public String description() {
		return "Craft @TOTAL@ essence.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(5510), new Item(5512));
	}

	@Override
	public Integer goal() {
		return 500;
	}

	@Override
	public Class<?>[] requiredToComplete() {
		return new Class<?>[] { Novice_Runecrafter.class };
	}
}
