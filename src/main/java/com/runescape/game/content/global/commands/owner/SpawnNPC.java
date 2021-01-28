package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class SpawnNPC extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "npc";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		World.spawnNPC(Integer.parseInt(cmd[1]), player, -1, true, true);
	}

}
