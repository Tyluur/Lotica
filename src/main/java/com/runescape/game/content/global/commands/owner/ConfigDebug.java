package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/20/2016
 */
public class ConfigDebug extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "config";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getPackets().sendConfig(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
	}
}
