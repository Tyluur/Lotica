package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

public class KrilTsutsaroth extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 6203 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.getRandom(4) == 0) {
			switch (Utils.getRandom(8)) {
				case 0:
					npc.setNextForceTalk(new ForceTalk("Attack them, you dogs!"));
					npc.playSound(3278, 2);
					break;
				case 1:
					npc.setNextForceTalk(new ForceTalk("Forward!"));
					npc.playSound(3276, 2);
					break;
				case 2:
					npc.setNextForceTalk(new ForceTalk("Death to Saradomin's dogs!"));
					npc.playSound(3277, 2);
					break;
				case 3:
					npc.setNextForceTalk(new ForceTalk("Kill them, you cowards!"));
					npc.playSound(3290, 2);
					break;
				case 4:
					npc.setNextForceTalk(new ForceTalk("The Dark One will have their souls!"));
					npc.playSound(3280, 2);
					break;
				case 5:
					npc.setNextForceTalk(new ForceTalk("Zamorak curse them!"));
					npc.playSound(3270, 2);
					break;
				case 6:
					npc.setNextForceTalk(new ForceTalk("Rend them limb from limb!"));
					npc.playSound(3273, 2);
					break;
				case 7:
					npc.setNextForceTalk(new ForceTalk("No retreat!"));
					npc.playSound(3276, 2);
					break;
				case 8:
					npc.setNextForceTalk(new ForceTalk("Flay them all!"));
					npc.playSound(3286, 2);
					break;
			}
		}
		int attackStyle = Utils.getRandom(2);
		switch (attackStyle) {
			case 0:// magic flame attack
				npc.setNextAnimation(new Animation(14384));
				for (Entity t : npc.getPossibleTargets()) {
					delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitions.MAGE, t)));
					World.sendProjectile(npc, t, 1211, 41, 16, 41, 35, 16, 0);
					if (Utils.getRandom(4) == 0)
						t.getPoison().makePoisoned(168);
				}
				break;
			case 1:// main attack
			case 2:// melee attack
				int damage = 463;// normal
				for (Entity e : npc.getPossibleTargets()) {
					if (e instanceof Player && (((Player) e).getPrayer().usingPrayer(0, 19) || ((Player) e).getPrayer().usingPrayer(1, 9)) && Utils.getRandom(6) == 0) {
						Player player = (Player) e;
						damage = 497;
						npc.setNextForceTalk(new ForceTalk("YARRRRRRR!"));
						npc.playSound(3274, 2);
						player.getPrayer().drainPrayer((Math.round(damage / 2)));
						player.setPrayerDelay(Utils.getRandom(5) + 5);
						player.getPackets().sendGameMessage("K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.");
					}
					npc.setNextAnimation(new Animation(damage <= 463 ? 14963 : 14384));
					delayHit(npc, 0, e, getMeleeHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitions.MELEE, e)));
				}
				break;
		}
		return defs.getAttackDelay();
	}
}
