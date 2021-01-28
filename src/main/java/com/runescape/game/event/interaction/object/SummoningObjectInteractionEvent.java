package com.runescape.game.event.interaction.object;

import com.runescape.game.content.skills.summoning.SummoningInfusion;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;
import com.runescape.utility.world.Coordinates;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class SummoningObjectInteractionEvent extends ObjectInteractionEvent {

	private static final WorldTile GOTO_LADDER_TILE = new WorldTile(3083, 3502, 0);

	@Override
	public int[] getKeys() {
		return new int[] { 28714, 28716, 1749 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		switch(object.getId()) {
		case 1749: // home climb down
			player.useStairs(828, Coordinates.SUMMONING_DUNGEON, 1, 2);
			break;
		case 28714: // summoning climb up
			player.useStairs(828, GOTO_LADDER_TILE, 1, 2);
			break;
		case 28716: // obelisk
			SummoningInfusion.openInfusionInterface(player);
			break;
		}
		return true;
	}

}
