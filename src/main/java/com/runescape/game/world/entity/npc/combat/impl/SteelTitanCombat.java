package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.utility.Utils;

public class SteelTitanCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 7344, 7343 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		boolean distant = false;
		int size = npc.getSize();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		int damage = 0;
		if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
			distant = true;
		if (usingSpecial) {// priority over regular attack
			npc.setNextAnimation(new Animation(8190));
			target.setNextGraphics(new Graphics(1449));
			if (distant) {// range hit
				delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.RANGE, target)));
			} else {// melee hit
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitions.MELEE, target)));
			}
		} else {
			if (distant) {
				int attackStage = Utils.getRandom(1);// 2
				switch (attackStage) {
				case 0:// magic
					damage = getRandomMaxHit(npc, 255, NPCCombatDefinitions.MAGE, target);
					npc.setNextAnimation(new Animation(7694));
					World.sendProjectile(npc, target, 1451, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getMagicHit(npc, damage));
					break;
				case 1:// range
					damage = getRandomMaxHit(npc, 244, NPCCombatDefinitions.RANGE, target);
					npc.setNextAnimation(new Animation(8190));
					World.sendProjectile(npc, target, 1445, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getRangeHit(npc, damage));
					break;
				}
			} else {// melee
				damage = getRandomMaxHit(npc, 244, NPCCombatDefinitions.MELEE, target);
				npc.setNextAnimation(new Animation(8183));
				delayHit(npc, 1, target, getMeleeHit(npc, damage));
			}
		}
		return defs.getAttackDelay();
	}
}
