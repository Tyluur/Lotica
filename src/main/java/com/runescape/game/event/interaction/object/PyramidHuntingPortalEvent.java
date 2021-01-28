package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * Created by Tyler on 6/19/2015.
 */
public class PyramidHuntingPortalEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { /*42219, 42220*/ };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (object.getId() == 42219) {
			player.setNextWorldTile(INSIDE_WORLD_TILE);
		} else {
			player.setNextWorldTile(OUTSIDE_WORLD_TILE);
		}
		return true;
	}

	/**
	 * The tile next to the portal to go inside
	 */
	public static final WorldTile OUTSIDE_WORLD_TILE = new WorldTile(3086, 3508, 0);

	/**
	 * The tile next to the portal to go outside
	 */
	public static final WorldTile INSIDE_WORLD_TILE = new WorldTile(1886, 3178, 0);
}
