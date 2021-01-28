package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class Auth extends CommandSkeleton<String> {
	@Override
	public String getIdentifiers() {
		return "auth";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		CoresManager.DATABASE_WORKER.submit(() -> {
			try {
				DatabaseFunctions.checkAuth(player, getCompleted(cmd, 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
