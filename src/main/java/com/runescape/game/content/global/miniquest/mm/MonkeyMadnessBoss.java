package com.runescape.game.content.global.miniquest.mm;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.quests.impl.MonkeyMadness;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class MonkeyMadnessBoss extends NPC {

	private static final long serialVersionUID = -6341045835455472435L;

	public MonkeyMadnessBoss(MonkeyMadnessController controller, WorldTile tile) {
		super(1472, tile);
		this.controller = controller;
		this.setIntelligentRouteFinder(true);
		this.setForceMultiArea(true);
		this.setForceAgressive(true);
		spawnMinions();
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
		if (!isEnraged() && getHitpoints() <= getMaxHitpoints() / 2) {
			setNextAnimation(new Animation(69));
//			setNextGraphics(new Graphics(2980, 0, 100));
			setNextForceTalk(new ForceTalk("AAAAAAAAAAAAAAAHRGH! YOU HAVE ENRAGED ME!"));
			setEnraged(true);
		}
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (source.isPlayer() && source.player().getControllerManager().verifyControlerForOperation(MonkeyMadnessController.class).isPresent()) {
			controller.leave(false);
			controller.getPlayer().getQuestManager().finishQuest(MonkeyMadness.class);
		}
	}
	
	/**
	 * Spawns all of the minions of the boss
	 */
	private void spawnMinions() {
		for (int i = 0; i < MINION_COUNT; i++) {
			getMinions().add(new MonkeyMadnessMinion(this));
		}
	}

	/**
	 * @return the enraged
	 */
	public boolean isEnraged() {
		return enraged;
	}

	/**
	 * @param enraged
	 * 		the enraged to set
	 */
	public void setEnraged(boolean enraged) {
		this.enraged = enraged;
	}

	/**
	 * @return the minions
	 */
	public List<MonkeyMadnessMinion> getMinions() {
		return minions;
	}

	/**
	 * The controller of this boss
	 */
	protected final MonkeyMadnessController controller;

	/**
	 * If the boss is enraged
	 */
	private boolean enraged;

	/**
	 * The list of minions of the boss
	 */
	private final List<MonkeyMadnessMinion> minions = new ArrayList<>();

	/**
	 * The amount of minions the boss gets
	 */
	private static final int MINION_COUNT = 5;

}
