package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class DagganothSupremeCombat extends CombatScript {
	
	@Override
	public Object[] getKeys() {
		return new Object[] { 2881, 2882, 2883 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = NPCDataLoader.getCombatDefinitions(npc.getId());
		int damage;
		switch (npc.getId()) {
			case 2881: // supreme (range)
				damage = getRandomMaxHit(npc, 300, NPCCombatDefinitions.RANGE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				npc.setNextGraphics(new Graphics(defs.getAttackGfx()));
				delayHit(npc, 2, target, getRangeHit(npc, damage));
				World.sendProjectile(npc, target, 475, 41, 16, 41, 35, 16, 0);
				break;
			case 2882: // prime (mage)
				damage = getRandomMaxHit(npc, 300, NPCCombatDefinitions.MAGE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				npc.setNextGraphics(new Graphics(defs.getAttackGfx()));
				delayHit(npc, 2, target, getMagicHit(npc, damage));
				World.sendProjectile(npc, target, 2707, 41, 16, 41, 35, 16, 0);
				break;
			case 2883: // rex (melee)
				damage = getRandomMaxHit(npc, 300, NPCCombatDefinitions.MELEE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, damage));
				break;
		}
		return defs.getAttackDelay();
	}
}
