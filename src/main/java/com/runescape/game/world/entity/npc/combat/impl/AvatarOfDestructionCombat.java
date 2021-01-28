package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/25/2015
 */
public class AvatarOfDestructionCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 8596 };
	}

	@Override
	public int attack(NPC npc, Entity baseTarget) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();

		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		int delay = defs.getAttackDelay();

		// special attack
		if (Utils.percentageChance(10)) {
			for (Entity target : npc.getPossibleTargets(true, true)) {
				// draining 90% of your prayer points
				if (target.isPlayer()) {
					target.player().getPrayer().drainPrayer((int) (target.player().getPrayer().getPrayerpoints() * 0.90));
					target.player().sendMessage("The Avatar of Destruction slices your prayer points away.");
				}
				//  can hit up to 70% of your health
				delayHit(npc, 2, target, getMeleeHit(npc, (int) (target.getHitpoints() * 0.70)));
				//  makes the target poisoned
				target.getPoison().makePoisoned(200);
			}
		} else {
			boolean melee = Utils.percentageChance(50);
			// regular melee attack attack
			for (Entity target : npc.getPossibleTargets(true, true)) {
				//  dealing up to 95% of your hp
				if (melee) {
					int maxHit = (int) (target.getHitpoints() * 0.95);
					delayHit(npc, 3, target, getMeleeHit(npc, maxHit));
				} else {
					delayHit(npc, 4, target, getRangeHit(npc, 300));
					target.setNextGraphics(new Graphics(1452));
					delay = delay + 1;
				}
				//  if we should close all prayers
				if (target.isPlayer() && Utils.percentageChance(10)) {
					target.player().getPrayer().closeAllPrayers();
				}
				//  if we should make you poisoned
				if (Utils.percentageChance(30)) {
					target.getPoison().makePoisoned(200);
				}
			}
		}
		return delay;
	}
}
