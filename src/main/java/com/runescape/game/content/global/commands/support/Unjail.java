package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class Unjail extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "unjail";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1).replaceAll("_", " ");
		final Player target = World.getPlayerByDisplayName(name);
		if (target == null) {
			player.sendMessage("No such player by the name: " + name);
			return;
		}
		target.getFacade().setJailedUntil(0);
	}
}
