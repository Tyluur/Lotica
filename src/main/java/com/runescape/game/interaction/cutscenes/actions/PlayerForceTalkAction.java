package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Player;

public class PlayerForceTalkAction extends CutsceneAction {

	private String text;

	public PlayerForceTalkAction(String text, int actionDelay) {
		super(-1, actionDelay);
		this.text = text;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextForceTalk(new ForceTalk(text));
	}

}
