package com.runescape.game.event.interaction.item;

import com.runescape.game.content.skills.DXPAlgorithms;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/27/2016
 */
public class ExperienceLampInteractionEvent extends ItemInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 4447 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue("Confirm", "Yes, enable 2x exp for " + DXPAlgorithms.MINUTES_FOR_DXP + " minutes", "Cancel");
			}

			@Override
			public void run(int interfaceId, int option) {
				if (option == FIRST) {
					player.getInventory().deleteItem(item);
					player.getFacade().setDoubleExperienceOverAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(DXPAlgorithms.MINUTES_FOR_DXP));
					sendItemDialogue(item.getId(), 1, "You rub the lamp and receive double", "experience bonuses for " + DXPAlgorithms.MINUTES_FOR_DXP + " minutes.");
					stage = -2;
				} else {
					end();
				}
			}

			@Override
			public void finish() {

			}
		});
		return true;
	}
}
