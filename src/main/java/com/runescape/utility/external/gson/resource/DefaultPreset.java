package com.runescape.utility.external.gson.resource;

import com.runescape.game.content.global.wilderness.presets.Preset;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/30/2015
 */
public class DefaultPreset {

	/**
	 * The name
	 */
	private final String name;

	/**
	 * The preset
	 */
	private final Preset preset;

	/**
	 * Constructs a new preset
	 *
	 * @param name
	 * 		The name of the preset
	 * @param preset
	 * 		The preset
	 */
	public DefaultPreset(String name, Preset preset) {
		this.name = name;
		this.preset = preset;
	}

	/**
	 * Gets the name of the preset
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the preset we're identifying
	 */
	public Preset getPreset() {
		return preset;
	}
}
