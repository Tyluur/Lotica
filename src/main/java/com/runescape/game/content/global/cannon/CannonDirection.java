package com.runescape.game.content.global.cannon;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Nov 30, 2013
 */
public enum CannonDirection {

	NORTH(0, 303),
	NORTH_EAST(1, 305),
	EAST(2, 307),
	SOUTH_EAST(3, 289),
	SOUTH(4, 184),
	SOUTH_WEST(5, 182),
	WEST(6, 178),
	NORTH_WEST(7, 291),
	TOP(8, 303);

	CannonDirection(int value, int animation) {
		this.value = value;
		this.animation = animation;
	}

	public int getValue() {
		return value;
	}

	public int getAnimation() {
		return animation;
	}

	private final int value;

	private final int animation;
}