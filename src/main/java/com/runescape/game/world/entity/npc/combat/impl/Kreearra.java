package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

public class Kreearra extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 6222 };
	}

	@Override
	public int attack(NPC npc, Entity baseTarget) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (!npc.isUnderCombat()) {
			npc.setNextAnimation(new Animation(6997));
			delayHit(npc, 1, baseTarget, getMeleeHit(npc, getRandomMaxHit(npc, 260, NPCCombatDefinitions.MELEE, baseTarget)));
			return defs.getAttackDelay();
		}
		npc.setNextAnimation(new Animation(6976));
		for (Entity target : npc.getPossibleTargets()) {
			if (Utils.getRandom(2) == 0) {
				sendMagicAttack(npc, target);
			} else {
				delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 720, NPCCombatDefinitions.RANGE, target)));
				World.sendProjectile(npc, target, 1197, 41, 16, 41, 35, 16, 0);
				WorldTile teleTile = null;
				for (int attempt = 0; attempt < 10; attempt++) {
					WorldTile possibleTile = new WorldTile(target, 2);
					if (World.canMoveNPC(possibleTile.getPlane(), possibleTile.getX(), possibleTile.getY(), target.getSize())) {
						teleTile = possibleTile;
						break;
					}
				}
				if (teleTile != null) { target.setNextWorldTile(teleTile); }
			}
		}
		return defs.getAttackDelay();
	}

	private void sendMagicAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(6976));
		for (Entity t : npc.getPossibleTargets()) {
			delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 210, NPCCombatDefinitions.MAGE, t)));
			World.sendProjectile(npc, t, 1198, 41, 16, 41, 35, 16, 0);
			target.setNextGraphics(new Graphics(1196));
		}
	}
}
