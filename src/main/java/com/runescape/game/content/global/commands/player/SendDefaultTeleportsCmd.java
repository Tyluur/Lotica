package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class SendDefaultTeleportsCmd extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "edge", "easts", "wests", "train", "pk", "mb" };
	}

	@Override
	public boolean shownOnInterface() {
		return false;
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String message = "Speak to the Wizard at home to teleport around the world.";
		player.getDialogueManager().startDialogue(SimpleItemMessage.class, 8007, message);
		player.setCloseInterfacesEvent(() -> player.sendMessage(message, 1));
	}
}
