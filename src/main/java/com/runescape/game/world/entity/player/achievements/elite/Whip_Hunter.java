package com.runescape.game.world.entity.player.achievements.elite;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Whip_Hunter extends AbstractAchievement {

	@Override
	public String description() {
		return "Receive @TOTAL@ abyssal whip drops from the demons.";
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 2_000_000));
	}

	@Override
	public Integer goal() {
		return 2;
	}
}
