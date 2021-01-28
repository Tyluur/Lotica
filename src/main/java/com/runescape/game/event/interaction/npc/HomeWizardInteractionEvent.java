package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.button.TeleportationSelectionInteractionEvent;
import com.runescape.game.event.interaction.button.TeleportationSelectionInteractionEvent.TransportationLocation;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class HomeWizardInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 1263 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
//		TravelDestinationInteractionEvent.sendDestinationSelection(player);
		if (option == ClickOption.FIRST) {
			TeleportationSelectionInteractionEvent.displaySelectionInterface(player, true);
		} else if (option == ClickOption.SECOND) {
			TransportationLocation last = player.getFacade().getLastTransportationLocation();
			if (last == null) {
				return true;
			}
			TeleportationSelectionInteractionEvent.teleportPlayer(player, last.getDestination(), () -> last.getLocations().handlePostTeleportation(player, last.getOptionIndex()));
		}
		return true;
	}
}
