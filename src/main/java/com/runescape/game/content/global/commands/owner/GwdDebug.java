package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.controllers.impl.GodWars;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 28, 2015
 */
public class GwdDebug extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "gwdd", "gwddebug" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (int i = 0; i < 4; i++) {
			player.getFacade().getGwdKillcount()[i] = 100;
		}
		player.getControllerManager().verifyControlerForOperation(GodWars.class).ifPresent(c -> {
			c.updateInterface();
		});
	}

}
