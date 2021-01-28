package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Rmospns extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "rmospns" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.putAttribute("removing_objects", !player.getAttribute("removing_objects", false));
		player.sendMessage("You are now " + (player.getAttribute("removing_objects", false) ? "removing" : "examining") + " objects.");
	}

}
