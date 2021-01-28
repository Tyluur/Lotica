package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Culinaromancer extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 3491 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int random = Utils.random(1, 4);
		// if we are too far to melee, we must range/mage
		while (isDistant(npc, target) && random == 1) {
			random = Utils.random(1, 4);
		}
		int damage = 0;
		switch(random) {
		case 1:
			npc.setNextAnimation(new Animation(15072));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 2: // range
			npc.setNextAnimation(new Animation(10504));
			damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.RANGE, target);
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 16, 41, 35, 16, 0);
			delayHit(npc, 2, target, getRangeHit(npc, damage));
			break;
		default:
		case 3: // mage
			damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target);
			npc.setNextForceTalk(new ForceTalk("Sallamakar Ro!"));
			npc.setNextAnimation(new Animation(1979));
			World.sendProjectile(npc, target, 368, 60, 32, 50, 50, 0, 0);
			delayHit(npc, 2, target, getMagicHit(npc, damage));
			if (damage > 0 && target.getFrozenBlockedDelay() < Utils.currentTimeMillis()) {
				if (target instanceof Player) {
					((Player) target).sendMessage("You have been frozen!");
				}
				target.addFreezeDelay(5000, true);
				target.setNextGraphics(new Graphics(369));
			}
			break;
		}
		return defs.getAttackDelay();
	}

}
