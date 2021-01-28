package com.runescape.game.content.global.commands.extreme_donator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class OpenBank extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "bank";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		// cant be in a controller or in an action
		boolean canBank = player.getControllerManager().getController() == null && player.getActionManager().getAction() == null;
		if (!canBank) {
			String message = "Bank is inaccessible during this activity.";
			player.getDialogueManager().startDialogue(SimpleMessage.class, message);
			player.sendMessage(message);
			return;
		}
		player.getBank().openBank();
	}

}
