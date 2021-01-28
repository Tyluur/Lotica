package com.runescape.game.content;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.player.Equipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 11/8/2015
 */
public class CostumeManager {

	/**
	 * The list of unlocked costumes
	 */
	private List<Costumes> unlockedCostumes = new ArrayList<>();

	/**
	 * The current costume the user is using
	 */
	private Costumes activeCostume;

	/**
	 * Gets the active costume
	 */
	public Costumes getActiveCostume() {
		return activeCostume;
	}

	/**
	 * Sets the active costume;
	 */
	public void setActiveCostume(Costumes costume) {
		this.activeCostume = costume;
	}

	/**
	 * Gets the model id of the costumes
	 *
	 * @param male
	 * 		If we are a male
	 * @param slotId
	 * 		The slot id we need the model ids of
	 */
	public int getCostumeModelId(boolean male, int slotId) {
		int[] modelIds = Costumes.getArrayForCostume(activeCostume, slotId);
		if (modelIds == null) {
			System.out.println("no array for " + activeCostume + " with slot: " + slotId);
			return -1;
		}
		int[] converted = new int[modelIds.length];
		converted[0] = ItemDefinitions.forId(modelIds[0]).getMaleEquip1();
		converted[1] = ItemDefinitions.forId(modelIds[1]).getFemaleEquip1();
		if (modelIds[0] == modelIds[1]) {
			converted[1] = converted[0];
		}
		System.out.println(Arrays.toString(modelIds) + ", " + Arrays.toString(converted));
		return male ? converted[0] : converted[1];
	}

	public enum Costumes {

		DERVISH_WHITE_COSTUME(new int[] { 20970, 21010 }, new int[] { 20980, 21020 }, new int[] { 20990, 21030 }, new int[] { 21000, 21000 });

		/**
		 * These arrays are the item ids for the corresponding slot, in the order of the male id first, then the female
		 * id
		 */
		private final int[] helmIds, bodyIds, legIds, feetIds;

		Costumes(int[] helmIds, int[] bodyIds, int[] legIds, int[] feetIds) {
			this.helmIds = helmIds;
			this.bodyIds = bodyIds;
			this.legIds = legIds;
			this.feetIds = feetIds;
		}

		public static int[] getArrayForCostume(Costumes costume, int slotId) {
			switch (slotId) {
				case Equipment.SLOT_HAT:
					return costume.helmIds;
				case Equipment.SLOT_CHEST:
					return costume.bodyIds;
				case Equipment.SLOT_LEGS:
					return costume.legIds;
				case Equipment.SLOT_FEET:
					return costume.feetIds;
			}
			return null;
		}

	}
}
