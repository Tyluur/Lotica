package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.dialogues.Dialogue;

public class LanderD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Are you sure you would like to leave the lander?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, get me out of here!", "No, I want to stay.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) { player.getControllerManager().forceStop(); }
			end();
		}
	}

	@Override
	public void finish() {

	}

}
