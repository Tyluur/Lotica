package com.runescape.game.event.interaction.button;

import com.runescape.game.content.economy.shopping.StoreInstance;
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
public class StoreInteractionEvent extends InterfaceInteractionEvent {
	@Override
	public int[] getKeys() {
		return new int[] { StoreInstance.STORE_INTERFACE_ID, StoreInstance.STORE_INVENTORY_ID };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int itemId, int packetId) {
		StoreInstance store = player.getAttribute("viewing_store");
		if (store == null) {
			return false;
		}
		switch (interfaceId) {
			case StoreInstance.STORE_INTERFACE_ID:
				Item item = store.getItemInSlot(slotId / 6);
				if (item == null) {
					return true;
				}
				if (componentId == 25) {
					switch (packetId) {
						case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
							store.sendValue(player, slotId, false);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
							store.buyItem(player, slotId, 1);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
							store.buyItem(player, slotId, 5);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
							store.buyItem(player, slotId, 10);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
							store.buyItem(player, slotId, 50);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON9_PACKET:
							store.buyItem(player, slotId, 500);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON8_PACKET:
							player.sendMessage(ItemInformationLoader.getExamine(item.getId()));
							break;
						default:
							break;
					}
				}
				break;
			case StoreInstance.STORE_INVENTORY_ID:
				if (componentId == 0) {
					switch (packetId) {
						case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
							store.sendValue(player, slotId, true);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
							store.sellItem(player, slotId, 1);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
							store.sellItem(player, slotId, 5);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
							store.sellItem(player, slotId, 10);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
							store.sellItem(player, slotId, Integer.MAX_VALUE);
							break;
						case WorldPacketsDecoder.ACTION_BUTTON9_PACKET:
							player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
								@Override
								public void handleInput() {
									store.sellItem(player, slotId, getInput());
								}
							});
							break;
						case WorldPacketsDecoder.ACTION_BUTTON6_PACKET:
							player.sendMessage(ItemInformationLoader.getExamine(player.getInventory().getItem(slotId).getId()));
							break;
						default:
							break;
					}
					break;
				}
		}
		return true;
	}
}
