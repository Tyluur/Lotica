package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

import java.util.Random;

@SuppressWarnings("serial")
public class Wolverine extends NPC {

	public Wolverine(Player target, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		setCombatLevel(target.getSkills().getCombatLevel() + new Random().nextInt(100) + 100);
		int hitpoints = 1000 + this.getCombatLevel() + target.getSkills().getCombatLevel() / 2 + new Random().nextInt(10);
		super.getCombatDefinitions().setHitpoints(hitpoints);
		setHitpoints(hitpoints);
		setWalkType(NORMAL_WALK);
		setForceAgressive(true);
		setAttackedBy(target);
		setAtMultiArea(true);
		faceEntity(target);
	}
}