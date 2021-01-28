package com.runescape.game.content.global.commands.owner;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Nn extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "nn" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			NPCDefinitions npcDefs = NPCDefinitions.getNPCDefinitions(i);
			if (npcDefs != null) {
				String nameRequested = cmd[1];
				String optionRequired = cmd.length == 3 ? cmd[2] : null;
				if (nameRequested.equalsIgnoreCase("-") || npcDefs.getName().toLowerCase().contains(nameRequested.replaceAll("_", " "))) {
					StringBuilder bldr = new StringBuilder();
					if (optionRequired != null) {
						if (npcDefs.hasOption(optionRequired)) {
							for (String o : npcDefs.options) {
								bldr.append(o + ", ");
							}
							player.getPackets().sendMessage(99, "NPC[id=" + i + ", lvl=" + npcDefs.combatLevel + ", name=" + npcDefs.getName() + ", size=" + npcDefs.size + ", options=" + bldr.toString() + "]", player);
						}
					} else {
						for (String o : npcDefs.options) {
							bldr.append(o + ", ");
						}
						player.getPackets().sendMessage(99, "NPC[id=" + i + ", lvl=" + npcDefs.combatLevel + ", name=" + npcDefs.getName() + ", size=" + npcDefs.size + ", options=" + bldr.toString() + "]", player);
					}
				}
			}
		}
	}

}
