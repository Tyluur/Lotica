package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.controllers.ControllerHandler;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Rlct extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "rlct";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		ControllerHandler.reload();
	}

}
