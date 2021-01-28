package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class God extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "god";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.putAttribute("god_mode", !player.getAttribute("god_mode", false));
		player.sendMessage("You are " + (player.getAttribute("god_mode", false) ? "on" : "off") + " god mode.");
	}

}
