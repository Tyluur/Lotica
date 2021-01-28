package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class SpinolypCombat extends CombatScript {
	
	@Override
	public Object[] getKeys() {
		return new Object[] { 2892, 2894, 2896 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target);
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 2, target, getMagicHit(npc, damage));
		World.sendProjectile(npc, target, 2703, 18, 18, 50, 50, 0, 0);
		// Projectile.create(attacker.getCentreLocation(),
		// victim.getCentreLocation(), 94, 45, 50, clientSpeed, 10, 35,
		// victim.getProjectileLockonIndex(), 10, 48);
		if (Utils.percentageChance(10)) {
			target.getPoison().makePoisoned(60);
		}
		return defs.getAttackDelay();
	}
}
