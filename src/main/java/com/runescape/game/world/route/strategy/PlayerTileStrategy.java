package com.runescape.game.world.route.strategy;

import com.runescape.game.world.route.RouteStrategy;

public class PlayerTileStrategy extends RouteStrategy {

	/**
	 * X position of tile.
	 */
	private int x;

	/**
	 * Y position of tile.
	 */
	private int y;

	public PlayerTileStrategy(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean canExit(int currentX, int currentY, int sizeXY, int[][] clip, int clipBaseX, int clipBaseY) {
		return RouteStrategy.checkFilledRectangularInteract(clip, currentX - clipBaseX, currentY - clipBaseY, sizeXY, sizeXY, x - clipBaseX, y - clipBaseY, 1, 1, 0);
	}

	@Override
	public int getApproxDestinationX() {
		return x;
	}

	@Override
	public int getApproxDestinationY() {
		return y;
	}

	@Override
	public int getApproxDestinationSizeX() {
		return 1;
	}

	@Override
	public int getApproxDestinationSizeY() {
		return 1;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FixedTileStrategy)) { return false; }
		PlayerTileStrategy strategy = (PlayerTileStrategy) other;
		return x == strategy.x && y == strategy.y;
	}

}
