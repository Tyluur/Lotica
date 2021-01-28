package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class Shops extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "shops", "shop"};
	}

	@Override
	public boolean shownOnInterface() {
		return false;
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.sendMessage("Visit the grand exchange at home to buy/sell any items.");
	}
}
