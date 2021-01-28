package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.controllers.impl.RunespanControler;
import com.runescape.game.interaction.dialogues.Dialogue;

public class RunespanPortalD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Where would you like to travel to?", "Low level entrance into the Runespan", "High level entrance into the Runespan");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		RunespanControler.enterRunespan(player, componentId == SECOND);
	}

	@Override
	public void finish() {

	}

}
