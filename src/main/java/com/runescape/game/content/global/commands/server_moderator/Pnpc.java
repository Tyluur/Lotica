package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class Pnpc extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "pnpc";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int npcId = Integer.parseInt(cmd[1]);
		player.getAppearence().transformIntoNPC(npcId);
		NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(npcId);
		System.out.println("Render emote for " + npcId + ":\t" + defs.renderEmote);
	}

}
