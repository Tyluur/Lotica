package com.runescape.game.world.entity.npc.godwars;

import com.runescape.game.world.WorldTile;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 28, 2015
 */
public enum Bosses {

	ARMADYL(26426, new WorldTile(2839, 5296, 2), new WorldTile(2839, 5295, 2), 20),
	BANDOS(26425, new WorldTile(2864, 5354, 2), new WorldTile(2863, 5354, 2), 20), 
	SARADOMIN(26427, new WorldTile(2907, 5265, 0), new WorldTile(2908, 5265, 0), 15), 
	ZAMORAK(26428, new WorldTile(2925, 5331, 2), new WorldTile(2925, 5332, 2), 10);

	/**
	 * The important integer values of the boss door
	 */
	private final int objectId, killCount;

	/**
	 * The important coordinates of the boss door
	 */
	private final WorldTile inside, outside;

	Bosses(int objectId, WorldTile inside, WorldTile outside, int killCount) {
		this.objectId = objectId;
		this.inside = inside;
		this.outside = outside;
		this.killCount = killCount;
	}

	/**
	 * @return the killCount
	 */
	public int getKillCount() {
		return killCount;
	}

	/**
	 * @return the objectId
	 */
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @return the outside
	 */
	public WorldTile getOutside() {
		return outside;
	}

	/**
	 * @return the inside
	 */
	public WorldTile getInside() {
		return inside;
	}

	/**
	 * Finds a boss door with the object id
	 * 
	 * @param objectId
	 *            The object id
	 * @return
	 */
	public static Bosses getBossDoor(int objectId) {
		for (Bosses door : Bosses.values()) {
			if (door.getObjectId() == objectId) {
				return door;
			}
		}
		return null;
	}
}