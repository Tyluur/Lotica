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
public abstract class AbstractTreasureTrail {

	/**
	 * Finds the type of treasure trail this is
	 */
	public abstract TreasureTrailType type();

	/**
	 * The type of action that must be performed to proceed onto the next stage
	 */
	public abstract TrailActionType actionType();

	/**
	 * The coordinates the trail is over at
	 */
	public abstract WorldTile coordinates();

	/**
	 * The id of the interface that will be shown
	 */
	public abstract int interfaceId();

	/**
	 * Constructs a new npc for the trail
	 *
	 * @param target
	 * 		The target
	 */
	public abstract TreasureTrailNPC fighterNPC(Player target);

	/**
	 * Finds the fighter npc
	 *
	 * @param target
	 * 		The target
	 * @param skipSpawnCheck
	 * 		If the spawn check should be skipped
	 */
	public TreasureTrailNPC findFighterNPC(Player target, boolean skipSpawnCheck) {
		if (skipSpawnCheck) {
			return null;
		}
		return fighterNPC(target);
	}

	/**
	 * The id of the double agent
	 */
	protected final int doubleAgentId = 5144;

}
