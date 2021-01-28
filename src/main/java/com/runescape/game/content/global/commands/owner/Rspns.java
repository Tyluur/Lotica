package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Rspns extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "rspns" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.putAttribute("removing_npcs", !player.getAttribute("removing_npcs", false));
		player.sendMessage("You are now " + (player.getAttribute("removing_npcs", false) ? "removing" : "examining") + " npcs.");
	}

}
