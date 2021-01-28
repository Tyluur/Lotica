package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class Mandrith extends Dialogue {

	@Override
	public void start() {
		npcId = getParam(0);
		sendOptionsDialogue(DEFAULT_OPTIONS, "Wilderness Point Shops", "Wilderness Supplies");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				switch (option) {
					case FIRST:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Main Store", "PvP Armour Store", "Brawling Gloves Store");
						stage = 0;
						break;
					case SECOND:
						openStore("Wilderness Supplies");
						break;
				}
				break;
			case 0:
				end();
				switch(option - 1) {
					case FIRST:
						openStore("Wilderness Point Store");
						break;
					case SECOND:
						openStore("PvP Armour Store");
						break;
					case THIRD:
						openStore("Brawling Gloves Store");
						break;
				}
				break;
			case 1:
				end();
				switch(option) {
					case FIRST:
						player.getPackets().requestClientInput(new InputEvent("Enter name:", InputEventType.NAME) {
							@Override
							public void handleInput() {
								String input = getInput();
								player.getFacade().getWildernessBlacklist().add(input);
								player.getDialogueManager().startDialogue(SimpleNPCMessage.class, npcId, input + " has now been added to your wilderness blacklist.");
							}
						});
						break;
					case SECOND:
						player.getPackets().requestClientInput(new InputEvent("Enter name:", InputEventType.NAME) {
							@Override
							public void handleInput() {
								String input = getInput();
								if (player.getFacade().getWildernessBlacklist().remove(input)) {
									player.getDialogueManager().startDialogue(SimpleNPCMessage.class, npcId, input + " has now been removed from your wilderness blacklist.");
								} else {
									player.getDialogueManager().startDialogue(SimpleNPCMessage.class, npcId, input + " was not in your blacklist to remove.");
								}
							}
						});
						break;
					case THIRD:
						Scrollable.sendQuestScroll(player, "My Blacklist", player.getFacade().getWildernessBlacklist().toArray(new String[player.getFacade().getWildernessBlacklist().size()]));
						break;
				}
				break;
		}
	}

	@Override
	public void finish() {

	}

	int npcId;

}
