package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class ToggleMasterSession extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "togglemaster";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getSession().setMasterSession(!player.getSession().isMasterSession());
		player.sendMessage(player.getSession().isMasterSession() ? "You are on master session." : "You are not on master session.");
	}
}
