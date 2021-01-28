package com.runescape.game.world.entity.player;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.item.FloorItem;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/14/2016
 */
public class RouteEvent {

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

	/**
	 * Constructs a new {@code RouteEvent} {@code Object}
	 *
	 * @param target
	 * 		The target we're travelling to
	 * @param task
	 * 		The {@code Runnable} task to execute
	 */
	public RouteEvent(Object target, Runnable task, Object... params) {
		this.target = target;
		this.destinationTile = getDestinationTile();
		this.task = task;

		int[] sizes = getSizes();
		this.sizeX = sizes[0];
		this.sizeY = sizes[1];
	}

	/**
	 * Constructs an array of the x and y sizes of the {@link #target}
	 */
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
	 * @param state
	 * 		The state, used only for floor items, to make pickup smoother
	 */
	public boolean processEvent(Player player, int state) {
		if (player.getPlane() != destinationTile.getPlane()) { return true; }
		// to ensure we've stopped walking
		if (player.hasWalkSteps() || player.getNextWalkDirection() != -1) {
			if (target instanceof FloorItem) {
				if (state == 0) {
					player.addWalkSteps(((FloorItem) target).getTile().getX(), ((FloorItem) target).getTile().getY());
					return processEvent(player, 1);
				}
			}
			return false;
		}
		if (checkReachability(player)) { return true; }
		// commences a tick after
		if (target instanceof FloorItem) {
			task.run();
		} else {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					task.run();
				}
			});
		}
		return true;
	}

	/**
	 * Checks if we can reach the destination
	 *
	 * @param player
	 * 		The player
	 */
	private boolean checkReachability(Player player) {
		List<WorldTile> tilesAround = getTilesAroundDestination();
		boolean tilesContains = false;
		for (WorldTile tile : tilesAround) {
			if (tile.matches(player)) {
				tilesContains = true;
			}
		}
		if (!tilesContains) {
			player.sendMessage("You can't reach that.");
			return true;
		}
		return false;
	}

	/**
	 * Gets the tiles around our {@link #destinationTile}
	 */
	private List<WorldTile> getTilesAroundDestination() {
		List<WorldTile> list = new ArrayList<>();
		boolean shouldRotate = false;
		if (target instanceof WorldObject) {
			int rotation = ((WorldObject) target).getRotation();
			if (rotation % 2 != 0) {
				shouldRotate = true;
			}
		}
		for (int deltaX = (shouldRotate ? -sizeY : -sizeX); deltaX <= (shouldRotate ? sizeY : sizeX); deltaX++) {
			for (int deltaY = (shouldRotate ? -sizeX : -sizeY); deltaY <= (shouldRotate ? sizeX : sizeY); deltaY++) {
				WorldTile deltaPosition = new WorldTile(destinationTile.getX() + deltaX, destinationTile.getY() + deltaY, destinationTile.getPlane());
				if (World.isFloorFree(deltaPosition.getPlane(), deltaPosition.getX(), deltaPosition.getY())) {
					list.add(deltaPosition);
				}
			}
		}
		list.add(destinationTile);
		return list;
	}
	
	/*
	private static List<WorldTile> getAdjacentWalkableTiles(WorldTile playerTile, WorldTile destination, Object target, int sizeX, int sizeY) {
		// if the object has been rotated
		boolean shouldRotate = false;
		if (target instanceof WorldObject) {
			int rotation = ((WorldObject) target).getRotation();
			if (rotation % 2 != 0) {
				shouldRotate = true;
			}
		}
		List<WorldTile> tiles = new ArrayList<>();
		for (int deltaX = (shouldRotate ? -sizeY : -sizeX); deltaX <= (shouldRotate ? sizeY : sizeX); deltaX++) {
			for (int deltaY = (shouldRotate ? -sizeX : -sizeY); deltaY <= (shouldRotate ? sizeX : sizeY); deltaY++) {
				WorldTile deltaPosition = new WorldTile(destination.getX() + deltaX, destination.getY() + deltaY, destination.getPlane());
				if (World.isFloorFree(deltaPosition.getPlane(), deltaPosition.getX(), deltaPosition.getY())) {
					tiles.add(deltaPosition);
				}
			}
		}
		if (target instanceof NPC || target instanceof FloorItem) {
			for (Iterator<WorldTile> iterator = tiles.iterator(); iterator.hasNext(); ) {
				WorldTile tile = iterator.next();
				if (target instanceof NPC && tile.matches(new WorldTile(3097, 3496, 0))) {
					continue;
				}
				verifyPathExists(playerTile, destination, iterator, tile);
			}
		} else if (target instanceof WorldObject) {
			for (Iterator<WorldTile> iterator = tiles.iterator(); iterator.hasNext(); ) {
				WorldTile tile = iterator.next();
				verifyPathExists(playerTile, destination, iterator, tile);
			}
		}
		tiles.add(destination);
		return tiles;
	}
	 */

}

