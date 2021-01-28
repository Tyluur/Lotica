package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

/**
 * 
 * @author Tyluur <itstyluur@gmail.com>
 * @since 2012-12-15
 */
public class DessousCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Dessous" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int random = Utils.random(1, 4);
		// if we are too far to melee, we must range/mage
		while (isDistant(npc, target) && random == 1) {
			random = Utils.random(1, 4);
		}
		switch (random) {
		case 1:
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		default:
			npc.setNextForceTalk(new ForceTalk("Hsssssssssss"));
			npc.setNextAnimation(new Animation(10501));
			target.applyHit(new Hit(target, 50, HitLook.REGULAR_DAMAGE));
			target.applyHit(new Hit(target, 50, HitLook.REGULAR_DAMAGE));
			break;
		}
		return defs.getAttackDelay();
	}

}
