package com.runescape.game.event.interaction.item;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.content.skills.slayer.SlayerTask;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class EnchantedGemInteractionEvent extends ItemInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 4155 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		SlayerTask task = player.getFacade().getSlayerTask();
		if (option == ClickOption.FIRST) {
			player.getDialogueManager().startDialogue(chatDialogue, task);
		} else {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 1597, task == null ? "You have no slayer task. Come to me to get one." : "You must slay " + task.getAmountToKill() + " more " + task.getName() + "s.");
		}
		return true;
	}

	private final Dialogue chatDialogue = new Dialogue() {

		int npcId = 1597;
		SlayerTask task;

		@Override
		public void start() {
			task = getParam(0);
			sendOptionsDialogue(DEFAULT_OPTIONS, "How many monsters do I have left?", "What combat level is my task?", "Turn off social slayer.");
		}

		@Override
		public void run(int interfaceId, int option) {
			if (task == null) {
				sendPlayerDialogue(CALM, "I don't have a Slayer task... Vannaka can probably give me one.");
				stage = -2;
				return;
			}
			switch (option) {
				case FIRST:
					sendNPCDialogue(npcId, CALM, "You must slay " + task.getAmountToKill() + " more " + task.getName() + "s.");
					stage = -2;
					break;
				case SECOND:
					NPCDefinitions definitions = NPCDefinitions.getNPCDefinitions(task.getName());
					if (definitions == null) {
						System.err.println("Error finding definitions for: " + task.getName() + "!");
						sendDialogue("Error. Please report on forums!");
						stage = -2;
						return;
					}
					sendDialogue("Your task's combat level is " + definitions.combatLevel + ".");
					stage = -2;
					break;
				case THIRD:
					SlayerManagement.removeSocialSlayer(player);
					sendDialogue("Social slayer has just been turned off for you.");
					stage = -2;
					break;
			}
		}

		@Override
		public void finish() {

		}
	};
}
