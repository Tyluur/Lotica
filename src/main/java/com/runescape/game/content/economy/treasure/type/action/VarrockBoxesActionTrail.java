package com.runescape.game.content.economy.treasure.type.action;

import com.runescape.game.content.economy.treasure.TreasureTrailData.TrailActionType;
import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractActionTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class VarrockBoxesActionTrail extends AbstractActionTrail {

	@Override
	public int getInteractionId() {
		return 46236;
	}

	@Override
	public String[] information() {
		return new String[] { "Search the boxes in the house near the south entrance of Varrock." };
	}

	@Override
	public TrailActionType actionType() {
		return TrailActionType.CLICK_OBJECT;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3203, 3384, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
