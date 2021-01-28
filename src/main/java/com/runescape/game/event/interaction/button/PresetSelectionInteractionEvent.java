package com.runescape.game.event.interaction.button;

import com.runescape.game.content.global.wilderness.presets.Preset;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.PresetManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/27/2015
 */
public class PresetSelectionInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 467 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		int presetsAllowed = player.getPresetManager().getPresetsAllowed();
		int highestIndex = PresetManager.INTERFACE_BUTTON_SLOTS[presetsAllowed - 1];
		if (componentId > highestIndex) {
			player.sendMessage("You don't have access to that many presets. Donate for more preset slots.");
			return true;
		}
		if (player.getControllerManager().getController() != null) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You cannot alter presets right now...");
			return true;
		}
		int indexClicked = -1;
		for (int i = 0; i < PresetManager.INTERFACE_BUTTON_SLOTS.length; i++) {
			if (componentId == PresetManager.INTERFACE_BUTTON_SLOTS[i]) {
				indexClicked = i;
				break;
			}
		}
		if (indexClicked == -1) {
			player.sendMessage("Error occurred...");
			return true;
		}
		Preset preset = player.getPresetManager().getPresetAt(indexClicked);
		if (preset == null) {
			player.getDialogueManager().startDialogue(new Dialogue() {
				@Override
				public void start() {
					sendDialogue("Store your current gear into this preset slot?");
				}

				@Override
				public void run(int interfaceId, int option) {
					switch (stage) {
						case -1:
							sendOptionsDialogue(DEFAULT_OPTIONS, "Yes", "No");
							stage = 0;
							break;
						case 0:
							switch (option) {
								case FIRST:
									player.getPresetManager().storePreset();
									break;
								case SECOND:
									end();
									break;
							}
							break;
					}
				}

				@Override
				public void finish() {

				}
			});
		} else {
			player.getDialogueManager().startDialogue(new Dialogue() {

				Preset selectedPreset;

				int indexClicked;

				@Override
				public void start() {
					selectedPreset = getParam(0);
					indexClicked = getParam(1);
					sendOptionsDialogue(DEFAULT_OPTIONS, "Use preset.", "Update Preset", "Rename Preset", "Delete Preset.");
				}

				@Override
				public void run(int interfaceId, int option) {
					switch (stage) {
						case -1:
							switch (option) {
								case FIRST:
									player.getPresetManager().usePreset(selectedPreset);
									end();
									break;
								case SECOND:
									sendOptionsDialogue("Confirm preset update change?", "Yes - proceed", "Cancel");
									stage = 0;
									break;
								case THIRD:
									player.getPresetManager().renamePresetAt(indexClicked);
									stage = -2;
									end();
									break;
								case FOURTH:
									player.getPresetManager().deletePreset(selectedPreset);
									player.sendMessage("Preset successfully deleted!");
									end();
									break;
							}
							break;
						case 0:
							end();
							if (option == FIRST) {
								if (player.getPresetManager().updatePresetAt(indexClicked)) {
									player.sendMessage("Preset successfully updated!");
									player.getPresetManager().showPresetsInterface();
								}
							}
							break;
					}
				}

				@Override
				public void finish() {

				}
			}, preset, indexClicked);
		}
		return true;
	}
}
