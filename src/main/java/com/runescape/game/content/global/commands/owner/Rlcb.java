package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.npc.combat.CombatScriptsHandler;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Rlcb extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "rlcb";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		CombatScriptsHandler.init();
	}

}
