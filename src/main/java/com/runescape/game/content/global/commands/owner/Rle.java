package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Rle extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "rle";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		InteractionEventManager.initialize();
	}

}
