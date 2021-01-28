package com.runescape.game.world.item;

import com.runescape.game.world.WorldTile;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the timed appearance of floor items on the runescape world
 * map.
 * 
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class WorldFloorItems {

	/**
	 * Sets up the items that will appear to everyone in the world
	 */
	public static void setup() {
		TIMED_ITEMS.add(new TimedAppearanceItem(1573, 1, 3, new WorldTile(3152, 3401, 0)));
	}

	/**
	 * @return the timedItems
	 */
	public static List<TimedAppearanceItem> getTimedItems() {
		return TIMED_ITEMS;
	}

	/**
	 * The list of items that will appear to the world
	 */
	private static final List<TimedAppearanceItem> TIMED_ITEMS = new ArrayList<>();

	/**
	 * An item that has a timed appearance
	 * 
	 * @author Tyluur
	 */
	public static class TimedAppearanceItem {

		public TimedAppearanceItem(int itemId, int amount, int delay, WorldTile tile) {
			this.itemId = itemId;
			this.amount = amount;
			this.delay = delay;
			this.tile = tile;
		}

		/**
		 * @return the itemId
		 */
		public int getItemId() {
			return itemId;
		}

		/**
		 * @return the amount
		 */
		public int getAmount() {
			return amount;
		}

		/**
		 * @return the delay
		 */
		public int getDelay() {
			return delay;
		}

		/**
		 * @return the tile
		 */
		public WorldTile getTile() {
			return tile;
		}

		/**
		 * The id of the item that will be spawned
		 */
		private final int itemId;

		/**
		 * The amount of the item that will be spawned
		 */
		private final int amount;

		/**
		 * The amount of seconds the item will appear after it has been picked
		 * up
		 */
		private final int delay;

		/**
		 * The tile of the item
		 */
		private final WorldTile tile;
	}

}
