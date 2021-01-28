package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.world.entity.player.Player;


public class PlayerTransformAction extends CutsceneAction {

	private int npcId;

	public PlayerTransformAction(int npcId, int actionDelay) {
		super(-1, actionDelay);
		this.npcId = npcId;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.getAppearence().transformIntoNPC(npcId);
	}

}
