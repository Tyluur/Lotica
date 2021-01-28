package com.runescape.game.content.global.miniquest.rfd;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.quests.impl.RecipeForDisaster;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class RecipeForDisasterBoss extends NPC {
	
	/**
	 * @param id
	 *            The id of the boss
	 * @param tile
	 *            The tile the boss is spawned on
	 */
	public RecipeForDisasterBoss(int id, WorldTile tile, RecipeForDisasterController controller) {
		super(id, tile, -1, true);
		this.controller = controller;
		setForceAgressive(true);
		setTarget(controller.getPlayer());
		setSpawned(true);
		setNextAnimation(new Animation(-1));
		getCombat().process();
	}
	
	@Override
	public void drop() {
		controller.getPlayer().getQuestManager().addAttribute(RecipeForDisaster.class, RecipeForDisaster.KILLED_KEY, controller.getPlayer().getQuestManager().getAttribute(RecipeForDisaster.class, RecipeForDisaster.KILLED_KEY, 0D) + 1);
		controller.calculateNextBoss();
	}

	private final RecipeForDisasterController controller;
	private static final long serialVersionUID = -6936476676151755140L;

}
