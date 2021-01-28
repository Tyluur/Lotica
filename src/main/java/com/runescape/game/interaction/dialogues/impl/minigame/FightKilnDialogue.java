package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.dialogues.Dialogue;


public class FightKilnDialogue extends Dialogue {

	@Override
	public void start() {
		player.getLockManagement().lockAll();
		sendDialogue("You journey directly to the Kiln.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
		player.getControllerManager().startController("FightKilnControler", 0);
	}

}
