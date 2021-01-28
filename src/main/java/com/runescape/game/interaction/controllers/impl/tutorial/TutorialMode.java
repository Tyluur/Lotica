package com.runescape.game.interaction.controllers.impl.tutorial;

import com.runescape.utility.Utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/15/2016
 */
public enum TutorialMode {

	REGULAR(16, 11) {
		@Override
		public String getDescription() {
			return "The regular account mode - a good balance for all type of players.";
		}
	},

	IRONMAN(28, 13) {
		@Override
		public String getDescription() {
			return "The ironman account mode - a build with no player interaction required.";
		}
	},

	ULTIMATE_IRONMAN(4, 14) {
		@Override
		public String getDescription() {
			return "The ultimate ironman account mode - limited bank space<br> RS experience rates, and progress resets on death.";
		}
	};

	private final int lineId;

	private final int componentId;

	public abstract String getDescription();

	TutorialMode(int lineId, int componentId) {
		this.lineId = lineId;
		this.componentId = componentId;
	}

	public static TutorialMode getModeByComponent(int componentId) {
		for (TutorialMode mode : TutorialMode.values()) {
			if (mode.componentId == componentId) {
				return mode;
			}
		}
		return null;
	}

	public static Optional<TutorialMode> getByName(String name) {
		return Arrays.stream(values()).filter(mode -> mode.getFormattedName().equalsIgnoreCase(name)).findFirst();
	}

	public int getLineId() {
		return lineId;
	}

	public int getComponentId() {
		return componentId;
	}

	public String getFormattedName() {
		return Utils.formatPlayerNameForDisplay(name());
	}
}
