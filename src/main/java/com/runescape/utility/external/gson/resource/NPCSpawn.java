package com.runescape.utility.external.gson.resource;

import com.runescape.game.world.WorldTile;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader.Direction;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 15, 2015
 */
public class NPCSpawn {

	/**
	 * Constructs a new npc spawn
	 * 
	 * @param npcId
	 *            The id of the spawn
	 * @param tile
	 *            The tile of the spawn
	 * @param direction
	 *            The direction of the spawn
	 */
	public NPCSpawn(int npcId, WorldTile tile, Direction direction) {
		this.npcId = npcId;
		this.tile = tile;
		this.direction = direction;
	}

	/**
	 * Constructs a new npc spawn facing north
	 * 
	 * @param npcId
	 *            The id of the spawn
	 * @param tile
	 *            The tile of the spawn
	 */
	public NPCSpawn(int npcId, WorldTile tile) {
		this.npcId = npcId;
		this.tile = tile;
		this.direction = Direction.NORTH;
	}

	/**
	 * Constructs a new npc spawn
	 * 
	 * @param npcId
	 *            The id of the spawn
	 * @param x
	 *            The x coordinate of the tile of the spawn
	 * @param y
	 *            The y coordinate of the tile of the spawn
	 * @param z
	 *            The plane of the coordinate of the tile of the spawn
	 * @param direction
	 *            The direction of the spawn
	 */
	public NPCSpawn(int npcId, int x, int y, int z, Direction direction) {
		this.npcId = npcId;
		this.tile = new WorldTile(x, y, z);
		this.direction = direction;
	}

	/**
	 * Constructs a new npc spawn facing north
	 * 
	 * @param npcId
	 *            The id of the spawn
	 * @param x
	 *            The x coordinate of the tile of the spawn
	 * @param y
	 *            The y coordinate of the tile of the spawn
	 * @param z
	 *            The plane of the coordinate of the tile of the spawn
	 */
	public NPCSpawn(int npcId, int x, int y, int z) {
		this.npcId = npcId;
		this.tile = new WorldTile(x, y, z);
		this.direction = Direction.NORTH;
	}

	/**
	 * @return the npcId
	 */
	public int getNpcId() {
		return npcId;
	}

	/**
	 * @return the tile
	 */
	public WorldTile getTile() {
		return tile;
	}

	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * The id of the npc of this spawn
	 */
	private final int npcId;

	/**
	 * The tile of the spawn
	 */
	private final WorldTile tile;

	/**
	 * The direction the spawn is facing
	 */
	private final Direction direction;

}
