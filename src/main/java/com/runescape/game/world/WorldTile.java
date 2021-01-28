package com.runescape.game.world;

import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;

import java.io.Serializable;

public class WorldTile implements Serializable {

	private static final long serialVersionUID = -6567346497259686765L;

	private short x, y;

	private byte plane;

	@Override
	public String toString() {
		return "WorldTile[x=" + x + ", y=" + y + ", plane=" + plane + ", regionId=" + getRegionId() + "]";
	}

	public WorldTile(int x, int y, int plane) {
		this.x = (short) x;
		this.y = (short) y;
		this.plane = (byte) plane;
	}

	public WorldTile(WorldTile tile) {
		this.x = tile.x;
		this.y = tile.y;
		this.plane = tile.plane;
	}

	public WorldTile(WorldTile tile, int randomize) {
		this.x = (short) (tile.x + Utils.getRandom(randomize * 2) - randomize);
		this.y = (short) (tile.y + Utils.getRandom(randomize * 2) - randomize);
		this.plane = tile.plane;
	}

	public WorldTile(int hash) {
		this.x = (short) (hash >> 14 & 0x3fff);
		this.y = (short) (hash & 0x3fff);
		this.plane = (byte) (hash >> 28);
	}

	public WorldTile(int x, int y) {
		this.x = (short) x;
		this.y = (short) y;
		this.plane = (byte) 0;
	}

	public static int getCoordFaceX(int x, int sizeX, int sizeY, int rotation) {
		return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
	}

	public static int getCoordFaceY(int y, int sizeX, int sizeY, int rotation) {
		return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
	}

	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		x += xOffset;
		y += yOffset;
		plane += planeOffset;
	}

	public final void setLocation(WorldTile tile) {
		setLocation(tile.x, tile.y, tile.plane);
	}

	public final void setLocation(int x, int y, int plane) {
		this.x = (short) x;
		this.y = (short) y;
		this.plane = (byte) plane;
	}

	public int getX() {
		return x;
	}

	public void setX(short x) {
		this.x = x;
	}

	public int getXInRegion() {
		return x & 0x3F;
	}

	public int getYInRegion() {
		return y & 0x3F;
	}

	public int getY() {
		return y;
	}

	public void setY(short y) {
		this.y = y;
	}

	public int getPlane() {
		if (plane > 3) { return 3; }
		return plane;
	}

	public void setPlane(byte plane) {
		this.plane = plane;
	}

	public int getRegionId() {
		return ((getRegionX() << 8) + getRegionY());
	}

	public int getRegionX() {
		return (x >> 6);
	}

	public int getRegionY() {
		return (y >> 6);
	}

	public int getLocalX() {
		return getLocalX(this);
	}

	public int getLocalX(WorldTile tile) {
		return getLocalX(tile, 0);
	}

	public int getLocalX(WorldTile tile, int mapSize) {
		return x - 8 * (tile.getChunkX() - (GameConstants.MAP_SIZES[mapSize] >> 4));
	}

	public int getChunkX() {
		return (x >> 3);
	}

	public int getLocalY() {
		return getLocalY(this);
	}

	public int getLocalY(WorldTile tile) {
		return getLocalY(tile, 0);
	}

	public int getLocalY(WorldTile tile, int mapSize) {
		return y - 8 * (tile.getChunkY() - (GameConstants.MAP_SIZES[mapSize] >> 4));
	}

	public int getChunkY() {
		return (y >> 3);
	}

	public int getRegionHash() {
		return getRegionY() + (getRegionX() << 8) + (plane << 16);
	}

	public int getTileHash() {
		return y + (x << 14) + (plane << 28);
	}

	public boolean withinDistance(WorldTile tile, int distance) {
		if (tile.plane != plane) { return false; }
		int deltaX = tile.x - x, deltaY = tile.y - y;
		return deltaX <= distance && deltaX >= -distance && deltaY <= distance && deltaY >= -distance;
	}

	public boolean withinDistance(WorldTile tile) {
		if (tile.plane != plane) { return false; }
		// int deltaX = tile.x - x, deltaY = tile.y - y;
		return Math.abs(tile.x - x) <= 14 && Math.abs(tile.y - y) <= 14;// deltaX
		// <= 14
		// &&
		// deltaX
		// >=
		// -15
		// &&
		// deltaY
		// <= 14
		// &&
		// deltaY
		// >=
		// -15;
	}

	public int getCoordFaceX(int sizeX) {
		return getCoordFaceX(sizeX, -1, -1);
	}

	public int getCoordFaceX(int sizeX, int sizeY, int rotation) {
		return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
	}

	public int getCoordFaceY(int sizeY) {
		return getCoordFaceY(-1, sizeY, -1);
	}

	public int getCoordFaceY(int sizeX, int sizeY, int rotation) {
		return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
	}

	public WorldTile transform(int x, int y, int plane) {
		return new WorldTile(this.x + x, this.y + y, this.plane + plane);
	}

	/**
	 * Checks if this world tile's coordinates match the other world tile.
	 *
	 * @param other
	 * 		The world tile to compare with.
	 * @return {@code True} if so.
	 */
	public boolean matches(WorldTile other) {
		return x == other.x && y == other.y && plane == other.plane;
	}

	/**
	 * Checks if this world tile's coordinates match the other world tile.
	 *
	 * @param x
	 * 		The  x of the other
	 * @param y
	 * 		The y of the other
	 * @param plane
	 * 		The plane of the other
	 * @return {@code True} if so.
	 */
	public boolean matches(int x, int y, int plane) {
		return this.x == x && this.y == y && this.plane == plane;
	}
	
	public int getXInChunk() {
		return x & 0x7;
	}

	public int getYInChunk() {
		return y & 0x7;
	}

	/**
	 * @param topLeftX
	 * 		The top left x coordinate
	 * @param topLeftY
	 * 		The top left y coordinate
	 * @param bottomRightX
	 * 		The bottom right x coordinate
	 * @param bottomRightY
	 * 		The bottom right y coordinate
	 */
	public boolean inRectangle(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
		boolean withinX = x >= topLeftX && x <= bottomRightX;
		boolean withinY = y <= topLeftY && y >= bottomRightY;
		return withinX && withinY;
	}

	public boolean withinArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public WorldTile getWorldTile() {
		return new WorldTile(x, y, plane);
	}
}
