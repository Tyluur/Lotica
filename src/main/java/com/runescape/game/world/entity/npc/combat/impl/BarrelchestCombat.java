package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class BarrelchestCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 5666 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = NPCCombatDefinitions.RANGE;
		if (Utils.random(3) == 2) { attackStyle = NPCCombatDefinitions.MELEE; }
		while (isDistant(npc, target) && attackStyle == 0) {
			attackStyle = 1;
		}
		if (attackStyle == NPCCombatDefinitions.MELEE) {
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target)));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		} else {
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target)));
			npc.setNextAnimation(new Animation(5895));
		}
		if (Utils.getRandom(2) == 0) {
			sendSpecial(npc, target);
		} else if (Utils.getRandom(5) == 1) {
			smashGroundEffect(npc, target);
		} else if (Utils.random(5) == 1) {
			if (target.isPlayer()) {
				target.player().getPrayer().closeAllPrayers();
				target.player().getPrayer().drainPrayer(Utils.random(30, 70));
			}
		}
		return defs.getAttackDelay();
	}

	/**
	 * Sends the special attack
	 *
	 * @param npc
	 * 		The npc
	 * @param target
	 * 		The target
	 */
	private void sendSpecial(NPC npc, Entity target) {
		npc.faceEntity(target);
		npc.setNextAnimation(new Animation(5896));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 500, -1, target)));
	}

	/**
	 * Sends the smash ground attack
	 *
	 * @param npc
	 * 		The npc
	 * @param target
	 * 		The target
	 */
	private void smashGroundEffect(NPC npc, Entity target) {
		npc.faceEntity(target);
		npc.setNextAnimation(new Animation(5895));
		target.applyHit(new Hit(target, Utils.random(80, 200), HitLook.REGULAR_DAMAGE));
	}

}
