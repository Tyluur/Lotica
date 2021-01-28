package com.runescape.game.world.entity.npc.others.boss;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 7/14/2016
 */
public class DagganothKingNPC extends NPC {

	public DagganothKingNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceAgressive(true);
		setForceTargetDistance(32);
	}

}
