package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandHandler;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Rlc extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "rlc" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		CommandHandler.initialize();
	}

}
