package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.content.global.minigames.CastleWars;
import com.runescape.game.interaction.dialogues.Dialogue;

public class CastleWarsScoreboard extends Dialogue {

	@Override
	public void start() {
		CastleWars.viewScoreBoard(player);

	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();

	}

	@Override
	public void finish() {

	}

}
