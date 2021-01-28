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
public abstract class AbstractActionTrail extends AbstractTreasureTrail {

	/*
	 * The id of the entity we must interact with
	 * 
	 * @return
	 */
	public abstract int getInteractionId();

	/**
	 * The information about this trail
	 * 
	 * @return
	 */
	public abstract String[] information();

	@Override
	public abstract TrailActionType actionType();

	@Override
	public abstract WorldTile coordinates();

	@Override
	public abstract TreasureTrailNPC fighterNPC(Player target);

	@Override
	public TreasureTrailType type() {
		return TreasureTrailType.ACTION;
	}

	@Override
	public int interfaceId() {
		return 345;
	}
}
