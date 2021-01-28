package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;

public class RockCrabs extends NPC {

	/**
	 *
	 */
	private static final long serialVersionUID = -4621253301902072996L;
	private int realId;

	public RockCrabs(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		realId = id;
		setForceAgressive(true); //to ignore combat lvl
	}

	@Override
	public void setTarget(Entity entity) {
		if (realId == getId()) {
			transformIntoNPC(realId - 1);
			setHitpoints(getMaxHitpoints()); //rock/bulders have no hp
		}
		super.setTarget(entity);
	}

	@Override
	public void reset() {
		setNPC(realId);
		super.reset();
	}

}
