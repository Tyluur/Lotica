package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader.Direction;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class StoreNPC extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "storen", "n" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int npcId = Integer.parseInt(cmd[1]);
		Direction direction = cmd.length == 3 ? Direction.valueOf(cmd[2]) : Direction.NORTH;
		try {
			NPCSpawnLoader.addSpawn(npcId, player.getWorldTile(), direction);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
