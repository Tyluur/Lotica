package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class JadCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 2745, 15208 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = isDistant(npc, target) ? Utils.percentageChance(50) ? 1 : 0 : Utils.random(3);
		if (attackStyle == 2) { //melee
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
				attackStyle = Utils.random(2);
			} else {
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 1, target, getMeleeHit(npc, target.getHitpoints()));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle == 1) { //range
			npc.setNextAnimation(new Animation(9276));
			npc.setNextGraphics(new Graphics(1625));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(451, 0, 100));
					delayHit(npc, 0, target, getRangeHit(npc, target.getHitpoints()));
				}
			}, 2);
		} else {
			npc.setNextAnimation(new Animation(9300));
			npc.setNextGraphics(new Graphics(1626));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					World.sendProjectile(npc, target, 1627, 80, 30, 40, 20, 5, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							delayHit(npc, 0, target, getMagicHit(npc, target.getHitpoints()));
						}

					}, 1);
				}
			}, 1);
		}
		return defs.getAttackDelay() + 2;
	}

}
