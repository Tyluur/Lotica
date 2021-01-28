package com.runescape.game.content.global.misc;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/27/2016
 */
public class ItemColouring {

	/**
	 * The slot ids for colour items. Used only in {@link ItemColourData}
	 */
	public static final int YELLOW_SLOT = 0, BLUE_SLOT = 1, WHITE_SLOT = 2, GREEN_SLOT = 3, RED_SLOT = 4;

	/**
	 * Handles the usage of a dye on an item
	 *
	 * @param player
	 * 		The player
	 * @param itemUsed
	 * 		The item used
	 * @param itemUsedWith
	 * 		The item used with
	 */
	public static boolean useDyeOnItem(Player player, Item itemUsed, Item itemUsedWith) {
		ItemColourData data = ItemColourData.getColourData(itemUsed.getId());
		Item dyeItem;
		if (data == null) {
			data = ItemColourData.getColourData(itemUsedWith.getId());
			dyeItem = itemUsed;
		} else {
			dyeItem = itemUsedWith;
		}
		if (data == null) {
			return false;
		}
		int[] colouredIds = data.getColouredIds();
		String dyeName = dyeItem.getName().split(" ")[0].trim();
		int index = getIndexByName(dyeName.toLowerCase());
//		System.out.println("dyeName=" + dyeName + ", dyeItem=" + dyeItem + ", index=" + index + ", data=" + data + ", ids=" + Arrays.toString(colouredIds));
		if (index < 0 || index >= colouredIds.length) {
			player.sendMessage("You can't colour this item " + dyeName.toLowerCase() + ".");
			return true;
		}
		int colouredId = colouredIds[index];
		if (colouredId == -1) {
			player.sendMessage("You can't colour this item " + dyeName.toLowerCase() + ".");
			return true;
		}
		player.getInventory().deleteItem(dyeItem.equals(itemUsed) ? itemUsedWith : itemUsed);
		player.getInventory().deleteItem(dyeItem);
		player.getInventory().addItem(colouredId, 1);
		return true;
	}

	/**
	 * Gets the index of the colour by the name of the dye
	 *
	 * @param name
	 * 		The name of the dye colour
	 */
	private static int getIndexByName(String name) {
		switch (name) {
			case "red":
				return RED_SLOT;
			case "blue":
				return BLUE_SLOT;
			case "green":
				return GREEN_SLOT;
			case "white":
				return WHITE_SLOT;
			case "yellow":
				return YELLOW_SLOT;
			default:
				return -1;
		}
	}

	public enum ItemColourData {

		// order is yellow blue white green red
		WHIP(4151, new int[] { 15441, 15442, 15443, 15444 }),
		STAFF_OF_LIGHT(15486, new int[] { 22209, 22211, -1, 22213, 22207 }),
		DARK_BOW(11235, new int[] { 15701, 15702, 15703, 15704, }),
		ROBIN_HOOD(2581, new int[] { 20950, 20951, 20952, -1, 20949 }),;

		private final int baseItemId;

		private final int[] colouredIds;

		ItemColourData(int baseItemId, int[] colouredIds) {
			this.baseItemId = baseItemId;
			this.colouredIds = colouredIds;
		}

		/**
		 * Gets the colour data for an item
		 *
		 * @param itemId
		 * 		The item
		 */
		public static ItemColourData getColourData(int itemId) {
			for (ItemColourData data : values()) {
				if (data.getBaseItemId() == itemId) {
					return data;
				}
			}
			return null;
		}

        public int getBaseItemId() {
            return this.baseItemId;
        }

        public int[] getColouredIds() {
            return this.colouredIds;
        }
    }

}
