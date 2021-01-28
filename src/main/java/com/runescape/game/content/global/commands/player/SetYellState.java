package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/1/2015
 */
public class SetYellState extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "toggleyell";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getFacade().setYellOff(!player.getFacade().isYellOff());
		player.sendMessage("You have now " + (player.getFacade().isYellOff() ? "dis" : "en") + "abled the yell channel.");
	}
}
