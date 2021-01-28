package com.runescape.game.event.interaction.object;

import com.runescape.game.GameConstants;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/6/2015
 */
public class KuradalsDungeonInteractionEvent extends ObjectInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 47236 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		Barriers barrier = Barriers.getBarrierByTile(object.getWorldTile());
		if (barrier == null) {
			if (GameConstants.DEBUG) {
				System.out.println("Could not find a barrier:\t" + object);
			}
			return true;
		}
		WorldTile[] coordinates = barrier.getTeleportCoordinates(player, object.getWorldTile());
		boolean shouldPassInfront = barrier.shouldPassInfront(player);
		player.setNextWorldTile(shouldPassInfront ? coordinates[0] : coordinates[1]);
		//System.out.println(barrier + ", " + shouldPassInfront);
		return true;
	}

	private enum Barriers {

		FIRST {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getX() == 1634 && (tile.getY() >= 5252 && tile.getY() <= 5254);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(1634, barrierTile.getY(), 0), new WorldTile(1635, barrierTile.getY(), 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getX() >= 1635;
			}
		},

		SECOND {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getY() == 5265 && (tile.getX() >= 1604 && tile.getX() <= 1606);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(barrierTile.getX(), 5265, 0), new WorldTile(barrierTile.getX(), 5264, 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getY() <= 5264;
			}
		},

		THIRD {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getY() == 5289 && (tile.getX() >= 1609 && tile.getX() <= 1611);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(barrierTile.getX(), 5289, 0), new WorldTile(barrierTile.getX(), 5288, 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getY() <= 5288;
			}
		},

		FOURTH {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getX() == 1625 && (tile.getY() <= 5303 && tile.getY() >= 5301);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(1626, barrierTile.getY(), 0), new WorldTile(1625, barrierTile.getY(), 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getX() <= 1625;
			}
		},

		FIFTH {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getX() == 1649 && (tile.getY() >= 5301 && tile.getY() <= 5303);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(1650, barrierTile.getY(), 0), new WorldTile(1649, barrierTile.getY(), 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getX() <= 1649;
			}
		},

		SIXTH {
			@Override
			public boolean isThisBarrier(WorldTile tile) {
				return tile.getY() == 5281 && (tile.getX() >= 1650 && tile.getX() <= 1652);
			}

			@Override
			public WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile) {
				return new WorldTile[] { new WorldTile(barrierTile.getX(), 5280, 0), new WorldTile(barrierTile.getX(), 5281, 0) };
			}

			@Override
			public boolean shouldPassInfront(WorldTile tile) {
				return tile.getY() >= 5281;
			}
		},;

		/**
		 * Checking if the object is this barrier
		 *
		 * @param tile
		 * 		The object's coordinates
		 */
		public abstract boolean isThisBarrier(WorldTile tile);

		/**
		 * Constructs an array of teleport coordinates when the barrier is passed. [0] = passing the barrier before
		 * we've past it, [1] = passing the barrier when we're already past it
		 *
		 * @param player
		 * 		The player
		 * @param barrierTile
		 * 		The tile of the barrier we're passing
		 */
		public abstract WorldTile[] getTeleportCoordinates(Player player, WorldTile barrierTile);

		/**
		 * Finds out if we should pass infront of the barrier
		 *
		 * @param tile
		 * 		The tile we're on
		 */
		public abstract boolean shouldPassInfront(WorldTile tile);

		/**
		 * This method gets a barrier by the tile
		 *
		 * @param tile
		 * 		The tile
		 */
		public static Barriers getBarrierByTile(WorldTile tile) {
			for (Barriers barriers : Barriers.values()) {
				if (barriers.isThisBarrier(tile)) {
					return barriers;
				}
			}
			return null;
		}

	}

}
