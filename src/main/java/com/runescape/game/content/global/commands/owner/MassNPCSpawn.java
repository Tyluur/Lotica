package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/29/2015
 */
public class MassNPCSpawn extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "mns";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int size = Integer.parseInt(cmd[1]);
		for (int i = 0; i < size; i++) {
			World.spawnNPC(1, player, -1, false, true);
		}
	}
}
