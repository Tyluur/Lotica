package com.runescape.game.world.entity.player.achievements.hard;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Tztok_Me extends AbstractAchievement {

	@Override
	public String description() {
		return "Kill the Jad";
	}

	@Override
	public Integer goal() {
		return 1;
	}

	@Override
	public void reward(Player player) {
		addItem(player, new Item(995, 350_000));
	}

	@Override
	public int interfaceItemId() {
		return 6528;
	}

}
