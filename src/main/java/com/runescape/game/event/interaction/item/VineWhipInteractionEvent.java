package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/3/2015
 */
public class VineWhipInteractionEvent extends ItemInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 21371 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		if (option == ClickOption.THIRD) {
			player.getInventory().deleteItem(item);
			player.getInventory().addItemDrop(21369, 1);
			player.getInventory().addItemDrop(4151, 1);
			player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), "You split the vine whip into two parts.");
			return true;
		}
		return false;
	}
}
