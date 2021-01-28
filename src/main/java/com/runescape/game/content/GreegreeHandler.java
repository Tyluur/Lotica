package com.runescape.game.content;

import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class GreegreeHandler {

	/**
	 * Handles the equipping of a possible greegree
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The item
	 */
	public static void handleGreegreeEquip(Player player, int itemId) {
		Greegree greegree = Greegree.getGreegreeByItem(itemId);
		if (greegree == null) {
			return;
		}
		Greegree current = player.getAttribute("greegree_worn");
		if (current == null) {
			player.putAttribute("greegree_worn", greegree);
		} else {
			transformBack(player);
			handleGreegreeEquip(player, itemId);
		}
	}

	/**
	 * Checks if the player should transform back into our original form
	 *
	 * @param player
	 * 		The player
	 * @param greegree
	 * 		The greegree currently equipped
	 */
	public static boolean shouldTransformBack(Player player, Greegree greegree) {
		Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
		boolean unequipped = weapon == null || (weapon.getId() != greegree.getItemId());
		boolean controller = player.getControllerManager().getController() != null;
		//System.out.println(unequipped + ", " + controller);
		return unequipped || controller;
	}

	/**
	 * Transforms the player back into its original form
	 *
	 * @param player
	 * 		The player
	 */
	public static void transformBack(Player player) {
		player.removeAttribute("greegree_worn");
		player.setNextGraphics(new Graphics(359));
		player.getAppearence().transformIntoNPC(-1);
	}

	public enum Greegree {

		MONKEY(4031, 1487),
		ZOMBIE(4029, 1485),
		NINJA(4024, 1480),
		GORILLA(4026, 1482);

		Greegree(int itemId, int npcId) {
			this.itemId = itemId;
			this.npcId = npcId;
		}

		/**
		 * The id of the gree gree
		 */
		private final int itemId;

		/**
		 * The id of the npc we transform to when we have this greegree equipped
		 */
		private final int npcId;

		/**
		 * Gets the greegree item id
		 */
		public int getItemId() {
			return itemId;
		}

		/**
		 * Gets the id of the npc we transform into
		 */
		public int getNpcId() {
			return npcId;
		}

		/**
		 * Finds a greegree with the specified item id
		 *
		 * @param itemId
		 * 		The id of the greegree
		 */
		public static Greegree getGreegreeByItem(int itemId) {
			for (Greegree greegree : Greegree.values()) {
				if (greegree.itemId == itemId) {
					return greegree;
				}
			}
			return null;
		}
	}
	
}
