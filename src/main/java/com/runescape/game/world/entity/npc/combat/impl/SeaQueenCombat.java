package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;

import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/20/2015
 */
public class SeaQueenCombat extends CombatScript {

	private static final Animation RANGED_ATTACK_ANIM = new Animation(3991);

	private static final Animation MAGIC_ATTACK_ANIM = new Animation(3992);

	private static final Graphics MAGIC_ATTACK_GRAPHICS = new Graphics(77);

	@Override
	public Object[] getKeys() {
		return new Object[] { 3847 };
	}

	// the possible skills that can be drained
	private final Integer[] combatSkills = new Integer[] { Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.PRAYER, Skills.MAGIC, Skills.RANGE };

	@Override
	public int attack(NPC npc, Entity singleTarget) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		// we can only use two styles of combat - mage/range
		boolean rangedAttack = Utils.percentageChance(50);
		List<Entity> targets = npc.getPossibleTargets();
		boolean special = Utils.percentageChance(20);
		if (rangedAttack) {
			if (special) {
				npc.setNextForceTalk(new ForceTalk("MORTAL!!!!"));
			}
			npc.setNextAnimation(RANGED_ATTACK_ANIM);
			targets.forEach(target -> {
				int numProjectiles = special ? 2 : 1;
				if (special) {
					if (target.isPlayer()) {
						// the amount of skills to drain
						int amountToDrain = 3;
						for (int i = 0; i < amountToDrain; i++) {
							// a random index from the array
							int skillId = Utils.randomArraySlot(combatSkills);
							int amountToDrainBy = Utils.random(10, 30);
							if (skillId == Skills.PRAYER) {
								target.player().getPrayer().drainPrayer(amountToDrainBy * 10);
							} else {
								target.player().getSkills().drainLevel(skillId, amountToDrainBy);
							}
							target.player().sendMessage("You feel your " + Skills.SKILL_NAME[skillId].toLowerCase() + " being drained...");
						}
					}
				}
				for (int i = 0; i < numProjectiles; i++) {
					World.sendProjectile(npc, target, 1099, 41, 16, 25, 35, 21, 0);
				}
				delayHit(npc, 3, target, getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
				if (special) {
					if (target.isPlayer()) {
						target.player().getPrayer().drainPrayer(target.player().getPrayer().getPrayerpoints());
					}
					delayHit(npc, 4, target, getRangeHit(npc, getRandomMaxHit(npc, 600, NPCCombatDefinitions.RANGE, target)));
				}
			});
		} else {
			npc.setNextAnimation(MAGIC_ATTACK_ANIM);
			if (special) {
				npc.setNextForceTalk(new ForceTalk("YOU STAND NO CHANCE!"));
			}
			targets.forEach(target -> {
				int amount = special ? Utils.random(1, 4) : 1;
				target.setNextGraphics(MAGIC_ATTACK_GRAPHICS);

				for (int i = 0; i < amount; i++) {
					World.sendProjectile(npc, target, 2706, 41, 16, 41, 35, 16, 0);
					delayHit(npc, special ? 6 : 3, target, getMagicHit(npc, getRandomMaxHit(npc, 500, NPCCombatDefinitions.MAGE, target)));
					if (target.isPlayer()) {
						target.player().getPrayer().drainPrayer(75);
						target.player().sendMessage("You feel your prayer being drained...");
					}
				}
			});
		}
		return defs.getAttackDelay();
	}

}
