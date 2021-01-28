package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.item.MembershipScrollD;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/22/2016
 */
public class GameScrollInteractionEvent extends ItemInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 3709, 18839, 18344 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
			return true;
		}
		switch(item.getId()) {
			case 3709: // membership
				player.getDialogueManager().startDialogue(MembershipScrollD.class);
				break;
			case 18839: // rigour
				if (!player.getFacade().getUnlockedPrayers()[0]) {
					player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), "You read the " + item.getName().toLowerCase() + " and suddenly", "feel more spiritually enlightened.");
					player.getFacade().getUnlockedPrayers()[0] = true;
					player.getInventory().deleteItem(item);
				} else {
					player.sendMessage("You have already unlocked rigour.");
				}
				break;
			case 18344: // augury
				if (!player.getFacade().getUnlockedPrayers()[1]) {
					player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), "You read the " + item.getName().toLowerCase() + " and suddenly", "feel more spiritually enlightened.");
					player.getFacade().getUnlockedPrayers()[1] = true;
					player.getInventory().deleteItem(item);
				} else {
					player.sendMessage("You have already unlocked augury.");
				}
				break;
		}
		return true;
	}
}
