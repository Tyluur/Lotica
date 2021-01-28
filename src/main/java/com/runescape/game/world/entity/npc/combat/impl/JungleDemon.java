package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.content.global.miniquest.mm.MonkeyMadnessBoss;
import com.runescape.game.content.global.miniquest.mm.MonkeyMadnessMinion;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class JungleDemon extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 1472 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		try {
			MonkeyMadnessBoss boss = null;
			boolean enraged = false;
			if (npc instanceof MonkeyMadnessBoss) {
				boss = (MonkeyMadnessBoss) npc;
				enraged = boss.isEnraged();
			}
			// if the boss is enraged, it will go full force. otherwise it
			// will hit low
			int random = Utils.random(1, 10);
			while (isDistant(npc, target) && random <= 3) {
				random = Utils.random(1, 10);
			}
			// random = 7;
			if (random <= 3) {
				npc.setNextAnimation(new Animation(64));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, enraged ? 350 : 200, defs.getAttackStyle(), target)));
				if (Utils.percentageChance(40) && target.isPlayer()) {
					Player player = target.player();
					if (player.getPrayer().hasPrayersOn()) {
						player.getPrayer().closeAllPrayers();
						player.sendMessage("<col=" + ChatColors.RED + ">Your prayers have been deactivated by the demon!", false);
					}
				}
			} else if (random > 3 && random <= 6) {
				// range
				npc.setNextAnimation(new Animation(69));
				target.setNextGraphics(new Graphics(1449));
				for (int i = 0; i < Utils.random(enraged ? 10 : 5, enraged ? 20 : 10); i++) {
					World.sendProjectile(npc, target, 1657, 30, 30, 75, 25, 0, 0);
					delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 70, NPCCombatDefinitions.RANGE, target)));
				}
			} else {
				// mage
				npc.setNextAnimation(new Animation(69));
				World.sendProjectile(npc, target, 2735, 18, 18, 50, 50, 0, 0);
				delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 200, NPCCombatDefinitions.MAGE, target)));
				if (Utils.percentageChance(30) && target.isPlayer()) {
					Player player = target.player();
					player.getPoison().makePoisoned(enraged ? 150 : 100);
				}
				if (enraged) {
					for (MonkeyMadnessMinion minion : boss.getMinions()) {
						if (minion == null) {
							continue;
						}
						World.sendProjectile(minion, target, 2735, 18, 18, 50, 50, 0, 0);
						delayHit(minion, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 50, NPCCombatDefinitions.MAGE, target)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defs.getAttackDelay();
	}

}
