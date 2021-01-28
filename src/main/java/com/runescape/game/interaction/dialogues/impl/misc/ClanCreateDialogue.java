package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 12, 2013
 */
public class ClanCreateDialogue extends Dialogue {

	@Override
	public void start() {
		sendDialogue("You must be a member of a clan in order to join their channel.", "Would you like to create a clan?");
	}

	@Override
	public void run(int interfaceId, int option) {
		if (stage == -1) {
			player.getPackets().requestClientInput(new InputEvent("Enter the clan name you'd like to have.", InputEventType.LONG_TEXT) {
				@Override
				public void handleInput() {
					ClansManager.createClan(player, getInput());
				}
			});
			end();
		}
	}

	@Override
	public void finish() {

	}

}
