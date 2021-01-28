package com.runescape.game.content.economy.treasure.type;

import com.runescape.game.content.economy.treasure.TreasureTrailData.TrailActionType;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TreasureTrailType;
import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.EmotesManager.Emotes;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public abstract class AbstractEmoteTrail extends AbstractTreasureTrail {

	/**
	 * The information the player needs to know about the trail
	 * 
	 * @return
	 */
	public abstract String[] information();

	/**
	 * Every emote trail has different requirements, whether it be armour
	 * equipped or completion of a quest. This method will define those
	 * requirements.
	 * 
	 * @param player
	 *            The player
	 */
	public abstract boolean passedRequirements(Player player);

	/**
	 * The emote we must perform
	 */
	public abstract Emotes emote();

	@Override
	public abstract WorldTile coordinates();

	@Override
	public abstract TreasureTrailNPC fighterNPC(Player target);

	@Override
	public int interfaceId() {
		return 345;
	}

	@Override
	public TreasureTrailType type() {
		return TreasureTrailType.EMOTE;
	}

	@Override
	public TrailActionType actionType() {
		return TrailActionType.PERFORM_EMOTE;
	}

}
