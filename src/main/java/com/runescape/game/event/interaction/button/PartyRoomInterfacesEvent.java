package com.runescape.game.event.interaction.button;

import com.runescape.game.content.PartyRoom;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class PartyRoomInterfacesEvent extends InterfaceInteractionEvent {
	@Override
	public int[] getKeys() {
		return new int[] { PartyRoom.PARTY_CHEST_INTERFACE, PartyRoom.INVENTORY_INTERFACE };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (!player.getAttribute("viewing_partyroom", false)) {
			//player.sendMessage("The party room is currently in development. Check back later.");
			return false;
		}
		switch (interfaceId) {
			case PartyRoom.PARTY_CHEST_INTERFACE:
				System.out.println(componentId);
				break;
			case PartyRoom.INVENTORY_INTERFACE:
				if (componentId == 0) {
					Item item = player.getInventory().getItem(slotId);
					switch (packetId) {
						case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
							addItem(player, item, 1);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
							addItem(player, item, 5);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
							addItem(player, item, 10);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
							addItem(player, item, Integer.MAX_VALUE);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
							player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
								@Override
								public void handleInput() {
									addItem(player, item, getInput());
								}
							});
							break;
						case WorldPacketsDecoder.ACTION_BUTTON8_PACKET:
							player.sendMessage(ItemInformationLoader.getExamine(item.getId()));
							break;
					}
				}
				break;
		}
		return true;
	}

	/**
	 * This method adds the item to the list of items to be dropped
	 *
	 * @param player
	 * 		The player adding the item
	 * @param item
	 * 		The item being added
	 * @param amount
	 * 		The amount of the item to add
	 */
	private void addItem(Player player, Item item, int amount) {
		if (amount > player.getInventory().getNumerOf(item.getId())) {
			amount = player.getInventory().getNumerOf(item.getId());
		}
		Item deposited = new Item(item.getId(), amount);
		if (!PartyRoom.addItem(deposited)) {
			player.sendMessage("These was an issue adding the item to the chest...");
		} else {
			player.getInventory().deleteItem(deposited);
		}
	}
}
