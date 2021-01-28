package com.runescape.game.world.entity.npc.dragons;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;

@SuppressWarnings("serial")
public class KingBlackDragon extends NPC {

	public KingBlackDragon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setForceTargetDistance(16);
	}

	public static boolean atKBD(WorldTile tile) {
		return (tile.getX() >= 2250 && tile.getX() <= 2292) && (tile.getY() >= 4675 && tile.getY() <= 4710);
	}

}
