package com.runescape.game.content.economy.treasure.type.map;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractMapTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class MorrtonMapTrail extends AbstractMapTrail {

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3433, 3265, 0);
	}

	@Override
	public int interfaceId() {
		return 341;
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
