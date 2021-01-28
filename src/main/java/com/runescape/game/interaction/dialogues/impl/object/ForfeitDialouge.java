package com.runescape.game.interaction.dialogues.impl.object;

import com.runescape.game.content.global.minigames.duel.DuelArena;
import com.runescape.game.interaction.dialogues.Dialogue;

public class ForfeitDialouge extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Forfeit Duel?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (componentId) {
		case OPTION_1:
			if (!player.getLastDuelRules().getRule(7)) {
				end();
				((DuelArena) player.getControllerManager().getController()).endDuel(player.getLastDuelRules().getTarget(), player);
			} else {
				sendDialogue("You can't forfeit during this duel.");
				stage = -2;
			}
			break;
		}
	}

	@Override
	public void finish() {

	}

}
