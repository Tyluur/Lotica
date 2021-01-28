package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/21/2016
 */
public class ChaosHandCannonDwarf extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 8776, 8777};
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		boolean melee = false;
		if (target.isPlayer()) {
			if (target.player().getPrayer().isRangeProtecting())
				melee = true;
		}
		if (melee) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			defs.setAttackStyle(NPCCombatDefinitions.MELEE);
		} else {
			npc.setNextAnimation(new Animation(12141));
			npc.setNextGraphics(new Graphics(2138));
			defs.setAttackStyle(NPCCombatDefinitions.RANGE);
		}
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), melee ? NPCCombatDefinitions.MELEE : NPCCombatDefinitions.RANGE, target);
		delayHit(npc, 2, target, !melee ? getRangeHit(npc, damage) : getMeleeHit(npc, damage));
		return defs.getAttackDelay();
	}
}
