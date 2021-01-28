package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.GameConstants;
import com.runescape.game.world.WorldTile;
import com.runescape.utility.Utils;

public interface PyramidHunterConstants {

	/**
	 * The coordinates for the pyramid hunter lobby area
	 */
	WorldTile LOBBY_ENTRANCE_COORDINATES = new WorldTile(1886, 3178, 0);

	/**
	 * The coordiantes that the player will go to when they spawn after the game
	 */
	WorldTile RESPAWN_LOBBY_COORDINATES = new WorldTile(1890, 3164, 0);

	/**
	 * The object id that represents the object which enters the user into a single-mode lobby
	 */
	int SINGLE_MODE_OBJECT_ID = 42030;

	/**
	 * The object id that represents the object which enters the user into a team-mode lobby
	 */
	int TEAM_MODE_OBJECT_ID = 42029;

	/**
	 * The object id that represents the object which starts the pyramid hunting game
	 */
	int ENTER_GAME_PORTAL_ID = 16050;

	/**
	 * The object id that represents the object which treasures are symbolized by
	 */
	int TREASURE_CHEST_ID = 37044;

	/**
	 * The array of possible available sarcophagus to search for supplies
	 */
	int[] SARCOPHAGUS_AVAILABLE = new int[] { 6514, 6517, 6516, 6513, 6512 };

	/**
	 * The array of possible locations that the monsters can be spawned at. These values will all be translated to
	 * different coordinates in dynamic regions.
	 */
	WorldTile[] MONSTER_SPAWNS = new WorldTile[] { new WorldTile(40, 47, 0), new WorldTile(49, 47, 0),
			                                                           new WorldTile(61, 46, 0),
			                                                           new WorldTile(55, 53, 0),
			                                                           new WorldTile(44, 53, 0),
			                                                           new WorldTile(34, 53, 0),
			                                                           new WorldTile(22, 54, 0),
			                                                           new WorldTile(24, 48, 0),
			                                                           new WorldTile(16, 48, 0),
			                                                           new WorldTile(10, 54, 0),
			                                                           new WorldTile(5, 49, 0),
			                                                           new WorldTile(12, 42, 0),
			                                                           new WorldTile(16, 42, 0),
			                                                           new WorldTile(22, 36, 0),
			                                                           new WorldTile(19, 34, 0),
			                                                           new WorldTile(7, 31, 0),
			                                                           new WorldTile(20, 30, 0),
			                                                           new WorldTile(33, 12, 0),
			                                                           new WorldTile(16, 4, 0), new WorldTile(6, 13, 0),
			                                                           new WorldTile(2, 4, 0), new WorldTile(2, 18, 0),
			                                                           new WorldTile(13, 19, 0),
			                                                           new WorldTile(42, 20, 0),
			                                                           new WorldTile(23, 21, 0),
			                                                           new WorldTile(35, 26, 0),
			                                                           new WorldTile(40, 7, 0), new WorldTile(61, 7, 0),
			                                                           new WorldTile(60, 21, 0),
			                                                           new WorldTile(50, 27, 0),
			                                                           new WorldTile(42, 36, 0),
			                                                           new WorldTile(57, 30, 0),
			                                                           new WorldTile(61, 37, 0),
			                                                           new WorldTile(48, 41, 0), };

	/**
	 * The array of possible WorldTiles that the treaure object can be spawned at. These values will be translated to
	 * the coordinates in the dynamic region.
	 */
	WorldTile[] TREASURE_SPAWNS = new WorldTile[] { new WorldTile(8, 33, 0), new WorldTile(23, 29, 0),
			                                                            new WorldTile(23, 44, 0),
			                                                            new WorldTile(34, 52, 0),
			                                                            new WorldTile(55, 41, 0),
			                                                            new WorldTile(33, 11, 0),
			                                                            new WorldTile(25, 9, 0),
			                                                            new WorldTile(46, 4, 0) };

	/**
	 * The minimum amount of scarabs per floor
	 */
	int BASE_SCARAB_COUNT = 5;

	/**
	 * This method returns the amount of food we must cook per level we're on. The total amount also varies based on the
	 * type of game we're playing (single/team)
	 *
	 * @param lobbyType
	 * 		The type of lobby we're in
	 * @param level
	 * 		The level we're on
	 */
	static int getFoodPerLevel(LobbyType lobbyType, int level) {
		if (GameConstants.DEBUG) {
			return 1;
		}
		switch (lobbyType) {
			case SINGLE:
				if (level < 5) {
					return 20;
				} else if (level > 5 && level <= 10) {
					return 28;
				} else {
					return 40;
				}
			case TEAM:
				if (level < 5) {
					return 30;
				} else if (level > 5 && level <= 10) {
					return 50;
				} else {
					return 65;
				}
		}
		return -1;
	}

	/**
	 * The amount of monsters that can be spawned on the floor. This is returned in an integer array of { min, max }
	 *
	 * @param level
	 * 		The level of the floor.
	 */
	static int[] getMonstersOnFloor(int level) {
		if (level < 5) {
			return new int[] { 5, 12 };
		} else if (level > 5 && level <= 10) {
			return new int[] { 12, 20 };
		} else if (level > 10 && level <= 20) {
			return new int[] { 20, 27 };
		} else {
			return new int[] { 27, 40 };
		}
	}

	/**
	 * Gets a random count of the monsters from the {@link #getMonstersOnFloor(int)} {@code Array}.
	 *
	 * @param level
	 * 		The level of the floor
	 */
	static int getRandomMonsterCount(int level) {
		int[] arrayPossibilities = getMonstersOnFloor(level);
		return Utils.random(arrayPossibilities[0], arrayPossibilities[1]);
	}

	/**
	 * Calculating the amount of points to give the player
	 *
	 * @param level
	 * 		The level
	 * @param gamePoints
	 * 		The points they received
	 * @param damageDealt
	 * 		The damage they've dealt
	 */
	static int getPointsToGive(int level, int gamePoints, int damageDealt) {
		double levelMultiplier = Math.rint(Math.floorDiv(level, 15));
		if (levelMultiplier == 0) {
			levelMultiplier = 1;
		}
		double pointsAddition = (gamePoints * 0.35) / 500;
		double damageExtra = (damageDealt * 0.05);

		int total = (int) Math.abs(levelMultiplier * (pointsAddition + damageExtra));

		if (total > 10_000) {
			total = 10_000 - Utils.random(500, 1000);
		}
		return total;
	}
	
}
