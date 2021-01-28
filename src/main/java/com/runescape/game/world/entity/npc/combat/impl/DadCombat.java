package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class DadCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 1125 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		if (Utils.random(10) >= 8) {
			List<WorldTile> destinations = new ArrayList<>();
			for (int i = 1; i <= 5; i++) {
				for (int j = -3; j <= 3; j++) {
					WorldTile tile = new WorldTile(target.getX() + j, target.getY() - i, target.getPlane());
					if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), target.getSize())) {
						destinations.add(tile);
					}
				}
			}
			Collections.sort(destinations, (o1, o2) -> Integer.compare(Utils.getDistance(target, o2), Utils.getDistance(target, o1)));
			if (!destinations.isEmpty()) {
				//TODO find push back emote
				target.setNextWorldTile(destinations.get(0));
				if (target.isPlayer()) { target.player().stopAll(); }
			}
		}
		return defs.getAttackDelay();
	}
}
