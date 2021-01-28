package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class SetWP extends CommandSkeleton<String> {
	@Override
	public String getIdentifiers() {
		return "setwp";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getFacade().setWildernessPoints(Integer.parseInt(cmd[1]));
	}
}
