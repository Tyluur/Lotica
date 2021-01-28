package com.runescape.game.world.entity.player.achievements.elite;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Corporeal_Punishment extends AbstractAchievement {

	@Override
	public String description() {
		return "Slay the corporeal beast @TOTAL@ times.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(3_000_000));
	}

	@Override
	public Integer goal() {
		return 100;
	}
}
