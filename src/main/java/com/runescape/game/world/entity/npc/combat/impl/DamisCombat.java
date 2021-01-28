package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * 
 * @author Tyluur <itstyluur@gmail.com>
 * @since 2012-12-15
 */
public class DamisCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 1975 };
	}

	private String[] MESSAGES = { "Taste my blade and feel my wrath!", "You are no match for my power!", "Your pathetic skills won't help you now!" };

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (target instanceof Player) {
			((Player) target).getPrayer().drainPrayer(40);
		}
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		npc.setNextForceTalk(new ForceTalk(MESSAGES[Utils.random(MESSAGES.length)]));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		return defs.getAttackDelay() - 1;
	}

}
