package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScriptsHandler;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class CND extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "cnd";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		CombatScriptsHandler.init();
		NPCDataLoader.CACHED_DATA.clear();
		for (NPC npc : World.getNPCs()) {
			if (npc == null) {
				continue;
			}
			npc.setCombatDefinitions(NPCDataLoader.getCombatDefinitions(npc.getId()));
			npc.setBonuses(NPCDataLoader.getBonuses(npc.getId()));
			npc.setHitpoints(npc.getMaxHitpoints());
		}
		player.sendMessage("All npc data has been reloaded");
	}

}
