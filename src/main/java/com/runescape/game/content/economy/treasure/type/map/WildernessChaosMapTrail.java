package com.runescape.game.content.economy.treasure.type.map;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractMapTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class WildernessChaosMapTrail extends AbstractMapTrail {

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3022, 3912, 0);
	}

	@Override
	public int interfaceId() {
		return 338;
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
