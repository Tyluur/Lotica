package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.TicketSystem;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/27/2015
 */
public class Ticket extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "ticket";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		TicketSystem.answerTicket(player);
	}
}
