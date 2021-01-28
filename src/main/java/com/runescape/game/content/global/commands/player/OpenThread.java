package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/15/2015
 */
public class OpenThread extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "thread", "openthread" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getPackets().sendOpenURL("http://www.lotica.soulplayps.com/forums/index.php?threads/." + Integer.parseInt(cmd[1]) + "/");
	}
}
