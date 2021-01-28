package com.runescape.game.world.entity.player.achievements.elite;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Cannonballer extends AbstractAchievement {

	@Override
	public String description() {
		return "Fire @TOTAL@ cannonballs.";
	}

	@Override
	public Integer goal() {
		return 5000;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 500_000));
	}

	@Override
	public int interfaceItemId() {
		return 2;
	}

}
