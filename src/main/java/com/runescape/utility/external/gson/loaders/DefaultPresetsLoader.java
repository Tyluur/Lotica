package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.game.content.global.wilderness.presets.Preset;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.resource.DefaultPreset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/30/2015
 */
public class DefaultPresetsLoader extends GsonCollections<DefaultPreset> {

	/**
	 * The map of default presets
	 */
	private static final List<DefaultPreset> DEFAULT_PRESETS = new ArrayList<>();

	/**
	 * The amount of presets the player gets
	 */
	private static final int PRESET_AMOUNT = 3;

	/**
	 * This method stores all items from the default presets into the player's bank, and stores the preset into the
	 * players' {@link com.runescape.game.world.entity.player.PresetManager}
	 *
	 * @param player
	 * 		The player
	 */
	public static void storeDefaultsInBank(Player player, boolean populateItems) {
		for (DefaultPreset preset : DEFAULT_PRESETS) {
			if (populateItems) {
				for (int i = 0; i < PRESET_AMOUNT; i++) {
					for (Item item : preset.getPreset().getInventoryItems()) {
						if (item == null) {
							continue;
						}
						player.getBank().addItem(item.getId(), item.getAmount(), true);
					}
					for (Item item : preset.getPreset().getEquippedItems()) {
						if (item == null) {
							continue;
						}
						player.getBank().addItem(item.getId(), item.getAmount(), true);
					}
				}
			}
			player.getPresetManager().insertPreset(preset.getName(), preset.getPreset());
		}
	}

	/**
	 * Stores a player's preset by name into our list of default presets
	 *
	 * @param player
	 * 		The player
	 * @param name
	 * 		The name of the preset the player has, which is going to be stored
	 */
	public void storePreset(Player player, String name) {
		Preset preset = player.getPresetManager().getPresetByName(name);
		if (preset == null) {
			throw new IllegalStateException("No preset found by name " + name);
		}
		List<DefaultPreset> defaultPresets = generateList();
		defaultPresets.add(new DefaultPreset(name, preset));
		save(defaultPresets);
		initialize();
	}

	@Override
	public void initialize() {
		DEFAULT_PRESETS.clear();
		DEFAULT_PRESETS.addAll(generateList().stream().collect(Collectors.toList()));
	}

	@Override
	public String getFileLocation() {
		return "data/resource/items/presets.json";
	}

	@Override
	public List<DefaultPreset> loadList() {
		return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<DefaultPreset>>() {}.getType());
	}

}
