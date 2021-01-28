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
public class SquireActionTrail extends AbstractActionTrail {

	@Override
	public int getInteractionId() {
		return 606;
	}

	@Override
	public String[] information() {
		return new String[] { "Talk to the Squire in the White Knights' castle in Falador." };
	}

	@Override
	public TrailActionType actionType() {
		return TrailActionType.CLICK_NPC;
	}

	@Override
	public WorldTile coordinates() {
		return null;
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
