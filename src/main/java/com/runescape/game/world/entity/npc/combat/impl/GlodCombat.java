package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/24/2016
 */
public class GlodCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 5996 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = defs.getAttackStyle();
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target)));

		if (npc.getHitpoints() <= (npc.getMaxHitpoints() * 0.30) && Utils.percentageChance(15)) {
			npc.setNextGraphics(new Graphics(444));
			npc.heal((int) (npc.getHitpoints() * 1.75));
			npc.setNextForceTalk(new ForceTalk("You can not defeat me!"));
		}

		if (Utils.percentageChance(5) && target.isPlayer()) {
			Player player = target.player();
			player.getPrayer().closeAllPrayers();
			player.getPrayer().drainPrayer((player.getSkills().getLevelForXp(Skills.PRAYER) / 6) * 10);
			npc.setNextForceTalk(new ForceTalk("Your prayers are meaningless..."));
		}

		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		return defs.getAttackDelay();
	}
}
