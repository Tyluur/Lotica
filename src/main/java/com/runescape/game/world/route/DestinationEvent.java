package com.runescape.game.world.route;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.FloorItem;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/14/2016
 */
public class DestinationEvent {

	/**
	 * The target object we are going to interact with
	 */
	private final Object target;

	/**
	 * The tile of the destination
	 */
	private final WorldTile destinationTile;

	/**
	 * The task to execute once we have arrived
	 */
	private final Runnable task;

	/**
	 * The sizes
	 */
	private final int sizeX, sizeY;

	public DestinationEvent(Object target, Runnable task) {
		this.target = target;
		this.destinationTile = getDestinationTile();
		this.task = task;

		int[] sizes = getSizes();
		this.sizeX = sizes[0];
		this.sizeY = sizes[1];
	}

	private int[] getSizes() {
		if (target instanceof Entity) {
			Entity e = (Entity) target;
			return new int[] { e.getSize(), e.getSize() };
		} else if (target instanceof WorldObject) {
			WorldObject o = (WorldObject) target;
			return new int[] { o.getDefinitions().getSizeX(), o.getDefinitions().getSizeY() };
		} else if (target instanceof FloorItem || target instanceof WorldTile) {
			return new int[] { 1, 1 };
		} else {
			throw new RuntimeException(target + " is not instanceof any reachable entity.");
		}
	}

	/**
	 * Gets the {@code WorldTile} instance of the {@link #target}
	 */
	public WorldTile getDestinationTile() {
		if (target instanceof Entity) {
			return ((Entity) target).getWorldTile();
		} else if (target instanceof WorldObject) {
			return ((WorldObject) target).getWorldTile();
		} else if (target instanceof FloorItem) {
			return ((FloorItem) target).getTile();
		} else if (target instanceof WorldTile) {
			return (WorldTile) target;
		} else {
			throw new RuntimeException(target + " is not instanceof any reachable entity.");
		}
	}

	/**
	 * This processes the event
	 *
	 * @param player
	 * 		The player
	 */
	public boolean processEvent(Player player) {
		if (player.getPlane() != destinationTile.getPlane()) { return true; }
		int distanceX = player.getX() - destinationTile.getX();
		int distanceY = player.getY() - destinationTile.getY();
		if (distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1) { return cantReach(player); }
		if (sizeX == 1 && sizeY == 1 && player.getX() != destinationTile.getX() && player.getY() != destinationTile.getY() && (player.hasWalkSteps() || player.getNextWalkDirection() != -1)) {
			return false;
		} else {
			player.resetWalkSteps();
			task.run();
			return true;
		}
	}

	/**
	 * Checks if a player can't reach the destination
	 *
	 * @param player
	 * 		The player
	 */
	public boolean cantReach(Player player) {
		// TODO properly check
		if (!player.hasWalkSteps() && player.getNextWalkDirection() == -1) {
			player.getPackets().sendGameMessage("You can't reach that.");
			return true;
		} else {
			return false;
		}
	}
}

