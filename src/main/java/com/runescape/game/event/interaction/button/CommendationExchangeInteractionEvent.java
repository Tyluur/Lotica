package com.runescape.game.event.interaction.button;

import com.runescape.game.content.global.minigames.pest.CommendationExchange;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/21/2016
 */
public class CommendationExchangeInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { CommendationExchange.INTERFACE };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		CommendationExchange.handleButtonOptions(player, componentId);
		return true;
	}
}
