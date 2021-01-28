package com.runescape.game.content.economy.treasure.type.map;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractMapTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class MiscellaniaMapTrail extends AbstractMapTrail {

	@Override
	public WorldTile coordinates() {
		return new WorldTile(2535, 3866, 0);
	}

	@Override
	public int interfaceId() {
		return 340;
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
