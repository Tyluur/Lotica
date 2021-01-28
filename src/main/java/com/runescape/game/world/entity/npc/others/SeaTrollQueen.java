package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/20/2015
 */
public class SeaTrollQueen extends NPC {

	public SeaTrollQueen(int id, WorldTile tile) {
		super(id, tile);
		setForceTargetDistance(16);
	}

}
