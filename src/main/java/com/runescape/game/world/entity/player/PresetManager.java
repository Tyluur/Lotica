package com.runescape.game.world.entity.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.wilderness.presets.Preset;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimplePlayerMessage;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.DefaultPresetsLoader;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/27/2015
 */
public class PresetManager {

	/**
	 * The slots that can be written over
	 */
	public static final int[] INTERFACE_SLOTS = new int[] { 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69, 71, 73 };

	/**
	 * The slots that are locked will be unlocked with these components
	 */
	public static final int[] LOCKED_INTERFACE_SLOTS = new int[] { 77, 79, 81, 83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109, 111, 113, 115, 117, 119, 121, 123, 125, 127, 129, 131, 133, 135 };

	/**
	 * The slots of the interface for clicking
	 */
	public static final int[] INTERFACE_BUTTON_SLOTS = new int[] { 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74 };

	/**
	 * If player skills from the preset should be set when activating a preset
	 */
	private static final boolean SET_SKILLS = false;

	/** The map of presets available */
	private Map<String, Preset> presets = new HashMap<>();

	/** The player managing the presets */
	private transient Player player;

	/**
	 * Sets the player
	 *
	 * @param player
	 * 		The player
	 */
	public PresetManager setPlayer(Player player) {
		this.player = player;
		return this;
	}

	public void clearPresets() {
		presets.clear();
	}

	/**
	 * This method stores the preset to the map of current presets.
	 */
	public void storePreset() {
		int presetSize = presets.size();
		int presetsAllowed = getPresetsAllowed();
		if (presetSize >= presetsAllowed) {
			player.sendMessage("You can only have " + presetsAllowed + " presets. Donate to raise this limit.");
			return;
		}
		player.getPackets().requestClientInput(new InputEvent("Enter Preset Name:", InputEventType.NAME) {
			@Override
			public void handleInput() {
				String presetName = getInput();
				insertPreset(presetName, new Preset().convertPreset(player));
				String message = "Preset successfully saved!";

				showPresetsInterface();

				player.sendMessage(message);
				player.getDialogueManager().startDialogue(SimplePlayerMessage.class, message);
			}
		});
	}

	/**
	 * The amount of presets allowed
	 */
	public int getPresetsAllowed() {
		if (!player.isAnyDonator()) {
			return 3;
		} else {
			if (player.hasPrivilegesOf(RightManager.ELITE_DONATOR)) {
				return 10;
			} else if (player.hasPrivilegesOf(RightManager.LEGENDARY_DONATOR)) {
				return 8;
			} else if (player.hasPrivilegesOf(RightManager.EXTREME_DONATOR)) {
				return 6;
			} else if (player.hasPrivilegesOf(RightManager.SUPREME_DONATOR, RightManager.DONATOR)) {
				return 5;
			}
			return 3;
		}
	}

	public void insertPreset(String name, Preset preset) {
		presets.put(name, preset);
	}

	/**
	 * Shows the preset selection interface
	 */
	public void showPresetsInterface() {
		int interfaceId = 467;
		player.closeInterfaces();

		int presetsAllowed = getPresetsAllowed();
		int highestIndex = LOCKED_INTERFACE_SLOTS[presetsAllowed - 1];

		for (int LOCKED_INTERFACE_SLOT : LOCKED_INTERFACE_SLOTS) {
			if (LOCKED_INTERFACE_SLOT > highestIndex) {
				player.getPackets().sendHideIComponent(interfaceId, LOCKED_INTERFACE_SLOT, false);
			}
		}

		int placeholderIndex = 0;

		for (int i = 0; i < getPresetsAllowed(); i++) {
			player.getPackets().sendIComponentText(interfaceId, INTERFACE_SLOTS[placeholderIndex], "<col=FFFFFF>Preset " + (i + 1) + ": Unused");
			placeholderIndex++;
		}

		int index = 0;
		for (Entry<String, Preset> entry : presets.entrySet()) {
			String presetName = entry.getKey();
			Preset preset = entry.getValue();

			StringBuilder presetInformation = new StringBuilder();
			presetInformation.append("Preset Name: ").append("<col=" + ChatColors.WHITE + ">" + presetName + "</col>").append("<br>Information:<col=" + ChatColors.YELLOW + "> ")./*append("Level: ").append(preset.getCombatLevel()).append(", ").*/append(preset.isOnCurses() ? "Curses" : "Moderns").append(", ").append(Utils.formatPlayerNameForDisplay(preset.getSpellbook().name())).append(" spellbook");

			player.getPackets().sendIComponentText(interfaceId, INTERFACE_SLOTS[index], presetInformation.toString());
			index++;
		}

		player.getPackets().sendIComponentText(interfaceId, 136, "Preset Manager");
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	/**
	 * This method updates the preset at a certain index with the current data
	 *
	 * @param index
	 * 		The index
	 */
	public boolean updatePresetAt(int index) {
		Entry<String, Preset> presetData = null;
		int baseIndex = 0;
		for (Entry<String, Preset> entry : presets.entrySet()) {
			if (index == baseIndex) {
				presetData = entry;
				break;
			}
			baseIndex++;
		}
		if (presetData == null) {
			return false;
		}
		Preset currentPreset = new Preset().convertPreset(player);
		presets.put(presetData.getKey(), currentPreset);
		return true;
	}

	/**
	 * Renames the preset at a certain index
	 *
	 * @param index
	 * 		The index
	 */
	public void renamePresetAt(int index) {
		Entry<String, Preset> presetData = null;
		int baseIndex = 0;
		for (Entry<String, Preset> entry : presets.entrySet()) {
			if (index == baseIndex) {
				presetData = entry;
				break;
			}
			baseIndex++;
		}
		if (presetData == null) {
			return;
		}
		String previousName = presetData.getKey();
		final Entry<String, Preset> finalPresetData = presetData;
		player.getPackets().requestClientInput(new InputEvent("Enter new preset name:", InputEventType.NAME) {
			@Override
			public void handleInput() {
				String newName = getInput();
				presets.remove(previousName);
				presets.put(newName, finalPresetData.getValue());
				showPresetsInterface();
				player.getDialogueManager().startDialogue(SimpleMessage.class, "The old preset called " + previousName + " was just renamed to " + newName + " successfully.");
			}
		});
	}

	/**
	 * Deletes a preset from the map of current presets
	 *
	 * @param preset
	 * 		The preset
	 */
	public void deletePreset(Preset preset) {
		Iterator<Entry<String, Preset>> it$ = presets.entrySet().iterator();
		boolean deleted = false;
		while (it$.hasNext()) {
			Entry<String, Preset> entry = it$.next();
			if (entry.getValue().equals(preset)) {
				it$.remove();
				deleted = true;
				break;
			}
		}

		if (!deleted) {
			System.out.println("Unable to delete a preset:\t" + preset);
		}

		showPresetsInterface();
	}

	/**
	 * This method changes all of the player's equipment to the preset's ones
	 *
	 * @param preset
	 * 		The preset to use
	 */
	public void usePreset(Preset preset) {
		if (!GameConstants.START_PLAYER_LOCATION.withinDistance(player, 32)) {
			player.sendMessage("You can only use presets near the designated home area.");
			return;
		}

		// ensuring we can store all the items in the bank
		if (!dumpAllToBank()) {
			player.sendMessage("Your bank is too full to use presets right now...");
			return;
		}

		List<Item> inventoryItems = preset.getInventoryItems();
		List<Item> equipmentItems = preset.getEquippedItems();

		Item notFound = null;
		int notFoundCount = 0;
		boolean missedRequirements = false;

		if (SET_SKILLS) {
			for (int skillId = 0; skillId < preset.getSkills().size(); skillId++) {
				double exp = preset.getSkills().get(skillId);
				if (skillId > 6) {
					break;
				}
				player.getSkills().setLevel(skillId, Skills.getLevelByExperience(exp, skillId));
				player.getSkills().setXp(skillId, exp);
				player.getSkills().restoreSkill(skillId);
			}
		}

		for (int i = 0; i < inventoryItems.size(); i++) {
			Item item = inventoryItems.get(i);
			if (item == null) {
				continue;
			}
			Item fromBank = removedItemFromBank(item);
			if (fromBank == null) {
				notFound = item;
				notFoundCount++;
				continue;
			}
			player.getInventory().getItems().set(i, fromBank);
		}
		k:
		for (int i = 0; i < equipmentItems.size(); i++) {
			Item item = equipmentItems.get(i);
			if (item == null) {
				continue;
			}
			HashMap<Integer, Integer> skillRequirements = item.getDefinitions().getWearingSkillRequirements();
			if (skillRequirements != null) {
				for (int skillId : skillRequirements.keySet()) {
					if (skillId > 24 || skillId < 0) {
						continue;
					}
					int level = skillRequirements.get(skillId);
					if (level < 0 || level > 120) {
						continue;
					}
					if (player.getSkills().getLevelForXp(skillId) < level) {
						missedRequirements = true;
						continue k;
					}
				}
			}
			Item fromBank = removedItemFromBank(item);
			if (fromBank == null) {
				notFound = item;
				notFoundCount++;
				continue;
			}
			player.getEquipment().getItems().set(i, fromBank);
		}

		List<String> messages = new ArrayList<>();

		if (notFound != null) {
			messages.add(notFoundCount + " item" + (notFoundCount == 1 ? "" : "s") + " " + (notFoundCount == 1 ? "was" : "were") + " not found from this preset...");
		} else {
			messages.add("All the items from this preset were available and are now worn.");
		}

		if (missedRequirements) {
			messages.add("You didn't have all the skill requirements to wield the items in this preset.");
		}

		player.getDialogueManager().startDialogue(SimplePlayerMessage.class, messages.toArray(new String[messages.size()]));

		player.getPrayer().setPrayerBook(preset.isOnCurses());
		player.getCombatDefinitions().setSpellBook(preset.getSpellbook().ordinal());
		player.getInventory().refresh();
		player.stopAll();

		for (int i = 0; i <= 14; i++) {
			player.getEquipment().refresh(i);
		}
		player.getAppearence().generateAppearenceData();
		player.sendMessage("You have fully loaded your preset '" + getPresetName(preset) + "'.");
	}

	/**
	 * This method dumps all the items worn and in equipment to the player's bank, if possible.
	 *
	 * @return True if the dump was successful (size in bank was fine)
	 */
	public boolean dumpAllToBank() {
		return player.getBank().depositAllInventory(false) && player.getBank().depositAllEquipment(false);
	}

	/**
	 * In order for presets to successfully store items, the player's equipment & inventory is dumped to the bank.
	 * Afterwards, the items are taken out of the bank if they exist.
	 *
	 * @return True if the item was removed from the bank
	 */
	private Item removedItemFromBank(Item itemToRemove) {
		return player.getBank().deleteItem(itemToRemove.getId(), itemToRemove.getAmount(), true);
	}

	/**
	 * Finds a preset at the specified index
	 *
	 * @param index
	 * 		The index
	 */
	public Preset getPresetAt(int index) {
		int baseIndex = 0;
		for (Entry<String, Preset> entry : presets.entrySet()) {
			if (index == baseIndex) {
				return entry.getValue();
			}
			baseIndex++;
		}
		return null;
	}

	/**
	 * Gets a preset by the name
	 *
	 * @param name
	 * 		The name
	 */
	public Preset getPresetByName(String name) {
		return presets.get(name);
	}

	/**
	 * Gets the name of the preset
	 *
	 * @param preset
	 * 		The preset
	 */
	public String getPresetName(Preset preset) {
		for (Entry<String, Preset> entry : presets.entrySet()) {
			if (entry.getValue().equals(preset)) {
				return entry.getKey();
			}
		}
		return "N/A";
	}

	/**
	 * Initializes default presets
	 *
	 * @param giveItems
	 * 		If we should give the player items from the presets
	 */
	public void initializeDefaults(boolean giveItems) {
		DefaultPresetsLoader.storeDefaultsInBank(player, giveItems);
	}
}
