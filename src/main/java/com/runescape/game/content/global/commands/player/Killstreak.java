package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/10/2015
 */
public class Killstreak extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "ks", "killstreak", "myks" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.setNextForceTalk(new ForceTalk("<col=" + ChatColors.RED + ">I AM CURRENTLY ON A KILLSTREAK OF " + player.getFacade().getKillstreak() + "!"));
	}
}
