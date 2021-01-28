package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader;

import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/26/2015
 */
public class ClearNPCSpawns extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "clearnpcs";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<Integer> localNPCs = World.getRegion(player.getRegionId()).getNPCsIndexes();
		for (Integer index : localNPCs) {
			NPC npc = World.getNPCs().get(index);
			if (npc == null) {
				continue;
			}
			NPCSpawnLoader.removeSpawn(npc);
			npc.finish();
		}
	}
}
