package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class Spec extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "spec";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getCombatDefinitions().restoreSpecialAttack(100);
	}

}
