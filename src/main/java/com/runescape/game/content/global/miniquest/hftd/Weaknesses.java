package com.runescape.game.content.global.miniquest.hftd;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 13, 2013
 */
enum Weaknesses {

	/**
	 * White
	 */
	AIR(1351),
	
	/**
	 * Blue
	 */
	WATER(1352),
	
	/**
	 * Brown
	 */
	EARTH(1354),
	
	/**
	 * Red
	 */
	FIRE(1353),
	
	/**
	 * Orange
	 */
	MELEE(1356),
	
	/**
	 * Green
	 */
	RANGED(1355);

	private final int id;
	
	Weaknesses(int id) {
		this.id = id;
	}

    public int getId() {
        return this.id;
    }
}
