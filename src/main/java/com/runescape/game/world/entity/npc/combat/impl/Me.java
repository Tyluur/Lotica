package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.content.global.miniquest.ld.LunarDiplomacyNPC;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Me extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Me" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		boolean nearDeath = npc.getHitpoints() <= 300;
		if (target.isPlayer()) {
			Player player = target.player();
			int attackStyle = PlayerCombat.isRanging(player);
			int spellId = player.getCombatDefinitions().getSpellId();
			while (isDistant(npc, target) && attackStyle == 0) {
				attackStyle = 1;
			}
			if (npc instanceof LunarDiplomacyNPC && player.getPrayer().hasPrayersOn() && Utils.percentageChance(40)) {
				player.getPrayer().closeAllPrayers();
				player.getPrayer().drainPrayer((player.getSkills().getLevelForXp(Skills.PRAYER) / 6) * 10);
				player.getLockManagement().lockActions(2000, LockType.INTERFACE_INTERACTION);
				npc.setNextForceTalk(new ForceTalk("Your prayers are meaningless..."));
			}
			if (spellId > 0 || Utils.random(5) == 3) {
				int damage = getRandomMaxHit(npc, 300, NPCCombatDefinitions.MAGE, target);
				npc.setNextAnimation(new Animation(1979));
				World.sendProjectile(npc, target, 368, 60, 32, 50, 50, 0, 0);
				delayHit(npc, 1, target, getMagicHit(npc, damage));
				if (damage > 0 && target.getFrozenBlockedDelay() < Utils.currentTimeMillis()) {
					if (target instanceof Player) {
						((Player) target).sendMessage("You have been frozen!");
					}
					target.addFreezeDelay(5000, true);
					target.setNextGraphics(new Graphics(369));
				}
				return 3;
			} else if (attackStyle == 0) {
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 2, target, getMeleeHit(npc, getRandomMaxHit(npc, nearDeath ? 400 : 100, NPCCombatDefinitions.RANGE, target)));
				return 2;
			} else {
				npc.setNextAnimation(new Animation(426));
				delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, nearDeath ? 500 : 250, NPCCombatDefinitions.RANGE, target)));
				World.sendProjectile(npc, target, 100, 34, 10, 30, 35, 16, 0);
				return 3;
			}
		}
		return defs.getAttackDelay();
	}

}
