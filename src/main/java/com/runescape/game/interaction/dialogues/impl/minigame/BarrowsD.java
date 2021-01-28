package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldTile;

public class BarrowsD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("You've found a hidden tunnel, do you want to enter?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, I'm fearless.", "No way, that looks scary!");
		} else if (stage == 0) {
			if (componentId == OPTION_1) { player.setNextWorldTile(new WorldTile(3534, 9677, 0)); }
			end();
		}
	}

	@Override
	public void finish() {

	}

}
