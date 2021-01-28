package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class BankpinInteractionEvent extends InterfaceInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 759 };
	}
	
	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		Integer i = player.getAttribute("pin_number_stage");
		int stage = 0;
		if (i == null) {
			player.putAttribute("pin_number_stage", 0);
		} else {
			stage = i;
		}
		int pinNumber = componentId / 4 - 1;
		if (i != null) {
			player.putAttribute("pin_number_stage", ++stage);
		}
		player.getPinManager().handlePinDigit(stage, pinNumber);
		return true;
	}
	
}
