package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class Setzp extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "setdp";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getFacade().setDreamPoints(Integer.parseInt(cmd[1]));
	}

}
