package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;

public class PlayerAnimationAction extends CutsceneAction {

	private Animation anim;

	public PlayerAnimationAction(Animation anim, int actionDelay) {
		super(-1, actionDelay);
		this.anim = anim;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextAnimation(anim);
	}

}
