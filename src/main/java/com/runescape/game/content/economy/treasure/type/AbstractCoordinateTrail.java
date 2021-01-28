package com.runescape.game.content.economy.treasure.type;

import com.runescape.game.content.economy.treasure.TreasureTrailData.TrailActionType;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TreasureTrailType;
import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public abstract class AbstractCoordinateTrail extends AbstractTreasureTrail {

	/**
	 * The information about the destination tile that will be sent over the
	 * {@link #interfaceId()}
	 * 
	 * @return
	 */
	public abstract String information();

	@Override
	public abstract WorldTile coordinates();

	@Override
	public abstract TreasureTrailNPC fighterNPC(Player target);

	@Override
	public TreasureTrailType type() {
		return TreasureTrailType.COORDINATE;
	}

	@Override
	public TrailActionType actionType() {
		return TrailActionType.DIG;
	}

	@Override
	public int interfaceId() {
		return 345;
	}

}
