package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.dialogues.DialogueHandler;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Rld extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "rld";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		DialogueHandler.init();
	}

}
