package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.*;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.others.PolyporeCreature;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class PolyporeCreatureCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 14688,
		                      14689,
		                      14690,
		                      14691,
		                      14692,
		                      14693,
		                      14694,
		                      14695,
		                      14696,
		                      14697,
		                      14698,
		                      14699,
		                      14700,
		                      14701 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		final PolyporeCreature creature = (PolyporeCreature) npc;
		if (Utils.isInRange(target.getX(), target.getY(), target.getSize(), npc.getX(), npc.getY(), npc.getSize(), 0) && creature.canInfect()) {
			int infectEmote = creature.getInfectEmote();
			if (infectEmote != -1) {
				npc.setNextAnimation(new Animation(infectEmote));
				if (target instanceof Player) {
					Player player = (Player) target;
					if (Utils.random(5) == 0) {
						player.getPrayer().drainPrayer(1);
						npc.setNextForceTalk(new ForceTalk("Krrr!"));
					}
					player.getPackets().sendGameMessage("The creature infests you with its toxic fungus.");
				}
				int base = npc.getId() >= 14696 && npc.getId() <= 14699 ? 10 : 2;
				delayHit(npc, 0, target, new Hit(npc, base + Utils.random(2), HitLook.POISON_DAMAGE));
				return 1;
			}
		}
		if ((npc.getId() == 14700 || npc.getId() == 14701) && Utils.random(5) == 0) {
			int size = npc.getSize();
			if (Utils.isInRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 6)) {
				int[][] dirs = Utils.getCoordOffsetsNear(size);
				for (int dir = 0; dir < dirs[0].length; dir++) {
					final WorldTile tile = new WorldTile(new WorldTile(target.getX() + dirs[0][dir], target.getY() + dirs[1][dir], target.getPlane()));
					if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), size)) {
						npc.setNextForceTalk(new ForceTalk("Hup!"));
						WorldTile middle = npc.getMiddleWorldTile();
						npc.setNextForceMovement(new NewForceMovement(new WorldTile(npc), 0, tile, 2, Utils.getFaceDirection(tile.getX() - middle.getX(), tile.getY() - middle.getY())));
						npc.setNextWorldTile(tile);
						npc.setNextAnimation(new Animation(15491));
						return 8;
					}
				}
			}
		} else if ((npc.getId() == 14688 || npc.getId() == 14689) && Utils.random(5) == 0) {
			npc.resetWalkSteps();
			npc.calcFollow(target, false);
			npc.setForceFollowClose(true);
			npc.setNextForceTalk(new ForceTalk("Raargh!"));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (!Utils.isInRange(target.getX(), target.getY(), target.getSize(), npc.getX(), npc.getY(), npc.getSize(), 0)) {
						npc.setNextForceTalk(new ForceTalk("*Sigh*"));
					}
					npc.setForceFollowClose(false);
				}
			}, 5);
			return 5;
		}
		if (def.getAttackStyle() != NPCCombatDefinitions.MELEE) {
			if (npc.getId() == 14701 || npc.getId() == 14689) {
				delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
				if (npc.getId() == 14689) {
					npc.setNextAnimation(new Animation(15487));
					return def.getAttackDelay();
				}
			} else {
				delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
				World.sendProjectile(npc, target, 2035, 41, 16, 20, 20, 16, 0);
			}
		} else {
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		}
		if (def.getAttackGfx() != -1) {
			npc.setNextGraphics(new Graphics(def.getAttackGfx()));
		}
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		return def.getAttackDelay();
	}
}
