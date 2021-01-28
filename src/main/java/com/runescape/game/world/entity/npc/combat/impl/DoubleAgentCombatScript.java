package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class DoubleAgentCombatScript extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Double agent" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target);
		if (damage != 0) {
			double perc = 1 - (npc.getHitpoints() / npc.getMaxHitpoints());
			damage += perc * 110;
		}
		boolean changedPray = false;
		if (Utils.percentageChance(25) && target.isPlayer() && target.player().getPrayer().hasPrayersOn()) {
			target.player().getPrayer().closeAllPrayers();
			npc.setNextForceTalk(new ForceTalk("Your prayers are useless against me."));
			changedPray = true;
		}
		npc.setNextAnimation(new Animation(changedPray ? 14307 : defs.getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, damage));
		return defs.getAttackDelay();
	}

}
