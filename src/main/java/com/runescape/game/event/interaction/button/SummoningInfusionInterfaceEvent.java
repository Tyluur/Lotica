package com.runescape.game.event.interaction.button;

import com.runescape.game.content.skills.summoning.SummoningInfusion;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class SummoningInfusionInterfaceEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 666, 672 };
	}
	
	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 672) {
			if (buttonId == 16) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
						@Override
						public void handleInput() {
							SummoningInfusion.handlePouchInfusion(player, slotId, getInput());
						}
					});
				}
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, Integer.MAX_VALUE);// x
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					SummoningInfusion.sendItemList(player, (boolean) player.getAttributes().get("infusing_scroll"), 1, slotId);
				}
			} else if (buttonId == 19) {
				SummoningInfusion.switchInfusionOption(player);
			}
		} else if (interfaceId == 666) {
			if (buttonId == 16) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					SummoningInfusion.handlePouchInfusion(player, slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {

						@Override
						public void handleInput() {
							SummoningInfusion.handlePouchInfusion(player, slotId, getInput());
						}
					});
				}
			} else if (buttonId == 18)
				SummoningInfusion.switchInfusionOption(player);
		}
		return true;
	}

}
