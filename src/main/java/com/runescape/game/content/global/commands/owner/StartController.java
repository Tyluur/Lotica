package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class StartController extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "startc", "startcontroller" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getControllerManager().startController(cmd[1], cmd.length == 3 ? cmd[2] : null);
	}
}
