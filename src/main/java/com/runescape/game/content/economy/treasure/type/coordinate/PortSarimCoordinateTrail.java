package com.runescape.game.content.economy.treasure.type.coordinate;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractCoordinateTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class PortSarimCoordinateTrail extends AbstractCoordinateTrail {

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3007, 3144, 0);
	}

	@Override
	public String information() {
		return "00 degrees 31 minutes south, 17 degrees 43 minutes east";
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}
}
