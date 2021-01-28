package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.dialogues.Dialogue;

public class DTSpectateReq extends Dialogue {

	@Override
	public void start() {
		sendDialogue(
				
				"You don't have the requirements to play this content, but you can",
				"spectate some of the matches taking place if you would like.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getDominionTower().openSpectate();
		end();
	}

	@Override
	public void finish() {

	}

}
