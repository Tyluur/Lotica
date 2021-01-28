package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.game.core.GameUpdateWorker;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/22/2016
 */
public class ChangeThreadDebugging extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "dbgworker"};
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		GameUpdateWorker.shouldPrintDebug.set(!GameUpdateWorker.shouldPrintDebug.get());
		String message = "Server is now " + (GameUpdateWorker.shouldPrintDebug.get() ? "printing debug slow cycles." : "hiding slow cycle debug messages.");
		player.sendMessage(message);
		System.out.println(message);
	}
}
