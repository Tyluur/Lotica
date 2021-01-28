package com.runescape.game.content.economy.treasure.type.coordinate;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractCoordinateTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class LumbridgeSwampCoordinateTrail extends AbstractCoordinateTrail {

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3184, 3149, 0);
	}

	@Override
	public String information() {
		return "00 degrees 20 minutes south, 23 degrees 15 minutes east";
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}
}
