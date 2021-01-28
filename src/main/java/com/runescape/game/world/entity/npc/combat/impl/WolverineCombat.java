package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;

import java.util.Random;

public class WolverineCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] {14899};
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final Player player = (Player) target;
		npc.setNextAnimation(new Animation(10961));
		int damage = player.getSkills().getCombatLevel() / 3 + new Random().nextInt(20) + 40;
		int dclaw1 = damage / 2;
		int dclaw2 = damage / 3;
		int dclaw3 = damage / 3;
		delayHit(npc, 2, target, getMeleeHit(npc, damage));
		delayHit(npc, 2, target, getMeleeHit(npc, dclaw1));
		delayHit(npc, 2, target, getMeleeHit(npc, dclaw2 / 10));
		delayHit(npc, 2, target, getMeleeHit(npc, dclaw3 / 10));
		return defs.getAttackDelay();
	}
}
