package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class Hide extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "hide";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getAppearence().switchHidden();
		player.sendMessage("You are now " + (player.getAppearence().isHidden() ? "hidden" : "visible") + " to all players.");
	}
}
