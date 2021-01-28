package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/26/2015
 */
public class Update extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "update", "shutdown" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		World.safeShutdown(Integer.parseInt(cmd[1]));
	}
}
