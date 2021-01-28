package com.runescape.game.content.global.loyalty;

import com.runescape.utility.Utils;

/**
 * Created by EliteBook on 11/14/2015.
 */
public enum LoyaltyEmotes {

	GOBLIN_BOW(22, 24),
	GOBLIN_SALUTE(23, 25),
	GLASS_BOX(24, 26),
	CLIMB_ROPE(25, 27),
	LEAN(26, 28),
	GLASS_WALL(27, 29),
	IDEA(28, 33),
	STOMP(29, 31),
	FLAP(30, 32),
	SLAP_HEAD(31, 30),
	ZOMBIE_WALK(32, 34),
	ZOMBIE_DANCE(33, 35),
	ZOMBIE_HAND(34, 36),
	SCARED(35, 37),
	BUNNY_HOP(36, 38),
	SNOWMAN_DANCE(38, 40),
	AIR_GUITAR(39, 41),
	SAFETY_FIRST(40, 42),
	EXPLORE(41, 43),
	TRICK(42, 44),
	FREEZE(43, 45),
	GIVE_THANKS(44, 46),

	;

	private final int buttonId, unlockId;

	LoyaltyEmotes(int buttonId) {
		this.buttonId = buttonId;
		this.unlockId = -1;
	}

	LoyaltyEmotes(int buttonId, int unlockId) {
		this.buttonId = buttonId;
		this.unlockId = unlockId;
	}

	public int getButtonId() {
		return buttonId;
	}

	public int getUnlockId() {
		return unlockId;
	}

	public static LoyaltyEmotes getRewardByName(String name) {
		for (LoyaltyEmotes emotes : LoyaltyEmotes.values()) {
			String emoteFormattedName = Utils.formatPlayerNameForDisplay(emotes.name());
			String nameFormatted = Utils.formatPlayerNameForDisplay(name);
			if (nameFormatted.equalsIgnoreCase(emoteFormattedName)) {
				return emotes;
			}
		}
		return null;
	}
}
