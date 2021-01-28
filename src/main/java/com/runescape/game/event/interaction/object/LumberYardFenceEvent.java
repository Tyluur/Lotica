package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class LumberYardFenceEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 31149 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		player.setNextWorldTile(new WorldTile(player.getX() == 3295 ? 3296 : 3295, player.getY(), player.getPlane()));
		return true;
	}

}
