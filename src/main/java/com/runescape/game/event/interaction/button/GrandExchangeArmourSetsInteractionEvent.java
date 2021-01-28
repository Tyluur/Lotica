package com.runescape.game.event.interaction.button;

import com.runescape.game.content.ArmourSets;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 6/28/2016
 */
public class GrandExchangeArmourSetsInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 645 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (componentId == 16) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				ArmourSets.sendComponents(player, slotId2);
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				ArmourSets.exchangeSet(player, slotId2);
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
				ArmourSets.examineSet(player, slotId2);
			}
		}
		return true;
	}
}
