package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.content.skills.Combat;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

public class KingBlackDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 50 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.getRandom(5);
		int size = npc.getSize();

		if (attackStyle == 0) {
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
				attackStyle = Utils.getRandom(4) + 1;
			} else {
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				return defs.getAttackDelay();
			}
		} else if (attackStyle == 1 || attackStyle == 2) {
			int damage = Utils.getRandom(650);
			final Player player = target instanceof Player ? (Player) target : null;
			if (Combat.hasAntiDragProtection(target) || (player != null && (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7)))) {
				damage = 0;
			}
			if (player != null && player.getFireImmune() > Utils.currentTimeMillis()) {
				if (damage != 0) {
					damage = Utils.getRandom(164);
				}
			} else if (damage == 0) {
				damage = Utils.getRandom(164);
			} else if (player != null) {
				player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
			}
			if (player != null) { player.increaseFireChargesIfPossible(); }
			delayHit(npc, 2, target, getRegularHit(npc, damage));
			World.sendProjectile(npc, target, 393, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(81));
		} else if (attackStyle == 3) {
			int damage;
			final Player player = target instanceof Player ? (Player) target : null;
			if (Combat.hasAntiDragProtection(target)) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				if (player != null) {
					player.getPackets().sendGameMessage("Your shield absorbs most of the dragon's poisonous breath!", true);
				}
			} else if (player != null && (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7))) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				player.getPackets().sendGameMessage("Your prayer absorbs most of the dragon's poisonous breath!", true);
			} else {
				damage = Utils.getRandom(650);
				if (player != null) {
					player.getPackets().sendGameMessage("You are hit by the dragon's poisonous breath!", true);
				}
			}
			if (Utils.getRandom(2) == 0) {
				target.getPoison().makePoisoned(80);
			}
			if (player != null) { player.increaseFireChargesIfPossible(); }
			delayHit(npc, 2, target, getRegularHit(npc, damage));
			World.sendProjectile(npc, target, 394, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(81));
		} else if (attackStyle == 4) {
			int damage;
			final Player player = target instanceof Player ? (Player) target : null;
			if (Combat.hasAntiDragProtection(target)) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				if (player != null) {
					player.getPackets().sendGameMessage("Your shield absorbs most of the dragon's freezing breath!", true);
				}
			} else if (player != null && (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7))) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				player.getPackets().sendGameMessage("Your prayer absorbs most of the dragon's freezing breath!", true);
			} else {
				damage = Utils.getRandom(650);
				if (player != null) {
					player.getPackets().sendGameMessage("You are hit by the dragon's freezing breath!", true);
				}
			}
			if (Utils.getRandom(2) == 0) {
				target.addFreezeDelay(15000);
			}
			if (player != null) { player.increaseFireChargesIfPossible(); }
			delayHit(npc, 2, target, getRegularHit(npc, damage));
			World.sendProjectile(npc, target, 395, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(81));
		} else {
			int damage;
			final Player player = target instanceof Player ? (Player) target : null;
			if (Combat.hasAntiDragProtection(target)) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				if (player != null) {
					player.getPackets().sendGameMessage("Your shield absorbs most of the dragon's shocking breath!", true);
				}
			} else if (player != null && (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7))) {
				damage = getRandomMaxHit(npc, 164, NPCCombatDefinitions.MAGE, target);
				player.getPackets().sendGameMessage("Your prayer absorbs most of the dragon's shocking breath!", true);
			} else {
				damage = Utils.getRandom(650);
				if (player != null) {
					player.getPackets().sendGameMessage("You are hit by the dragon's shocking breath!", true);
				}
			}
			if (player != null) { player.increaseFireChargesIfPossible(); }
			delayHit(npc, 2, target, getRegularHit(npc, damage));
			World.sendProjectile(npc, target, 396, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(81));
		}
		return defs.getAttackDelay();
	}
}
