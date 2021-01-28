package com.runescape.game.world.item;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 4, 2015
 */
public class ItemDegrading {

	/** An hour in ticks */
	private static final int HOUR = 6000;

	/**
	 * This number symbolizes the barrows armour has full charges
	 */
	private static final int FULL_BARROWS_CHARGES = 200;

	/**
	 * Finds the repair cost for all the items in the player's inventory
	 *
	 * @param player
	 * 		The player
	 */
	public static int getRepairCost(Player player) {
		int cost = 0;
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null || item.getDefinitions().isNoted()) {
				continue;
			}
			String name = item.getName();
			RepairDefinitions definitions = RepairDefinitions.getDefinitions(name);
			if (definitions == null) {
				continue;
			}
			int index = definitions.indexOf(name);
			if (index == -1) {
				continue;
			}
			String repairName = definitions.getNames()[index];
			if (repairName.equalsIgnoreCase(name)) {
				continue;
			}
			int price = definitions.getPrices()[index];
			cost += price;
		}
		return cost;
	}

	/**
	 * Repairs all broken items in the player's inventory to their fixed state
	 *
	 * @param player
	 * 		The player
	 * @return True if any item was repaired
	 */
	public static boolean repairAll(Player player) {
		boolean repaired = false;
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null || item.getDefinitions().isNoted()) {
				continue;
			}
			String name = item.getName();
			RepairDefinitions definitions = RepairDefinitions.getDefinitions(name);
			if (definitions == null) {
				continue;
			}
			int index = definitions.indexOf(name);
			if (index == -1) {
				continue;
			}
			int newId = definitions.getRepairIds()[index];
			player.getInventory().getItems().replace(player.getInventory().getItems().getThisItemSlot(item), newId, 1);
			repaired = true;
		}
		if (repaired) {
			player.getInventory().refresh();
		}
		return repaired;
	}

	/**
	 * Finds the next barrows id of the item
	 *
	 * @param currentId
	 * 		The current id of the barrows armour
	 * @param charges
	 * 		The charges the barrows armour has
	 */
	public static int getNextBarrowsId(int currentId, int charges) {
		String newName = "";
		ItemDefinitions definitions = ItemDefinitions.forId(currentId);
		if (charges == FULL_BARROWS_CHARGES) {
			newName = definitions.getName() + " 100";
		} else {
			String name = definitions.getName();
			for (char c : name.toCharArray()) {
				if (Character.isDigit(c)) {
					break;
				}
				newName += "" + c;
			}
			newName = newName.trim();
			newName += " " + (charges - 25);
		}
		ItemDefinitions definitions1 = ItemDefinitions.forName(newName);
		if (definitions1 == null) {
			System.err.println("Couldn't find definitions for item [newName=" + newName + "]");
			return -1;
		} else {
			return definitions1.getId();
		}
	}

	/**
	 * Finds the charges the barrows armour has
	 *
	 * @param itemId
	 * 		The id of the barrows item
	 */
	public static int getBarrowsCharges(int itemId) {
		String name = ItemDefinitions.forId(itemId).getName();
		String numbersFound = "";
		for (char c : name.toCharArray()) {
			if (Character.isDigit(c)) {
				numbersFound += "" + c;
			}
		}
		return Integer.valueOf(numbersFound);
	}

	public enum DegradeDefinitions {

		/**
		 * Full barrows armour degrades into its first phase right after being used in combat.
		 */
		FULL_BARROWS_ARMOUR(DegradeType.COMBATTED_DEGRADE, 1, 4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759) {
			@Override
			public int getNextDegradeId(int currentId) {
				return getNextBarrowsId(currentId, FULL_BARROWS_CHARGES);
			}
		},

		/**
		 * All degraded barrows armour has 15 minutes to degrade to its next phase
		 */
		DEGRADED_BARROWS_ARMOUR(DegradeType.COMBATTED_DEGRADE, 1500, 4856, 4857, 4858, 4859, 4862, 4863, 4864, 4865, 4868, 4869, 4870, 4871, 4874, 4875, 4876, 4877, 4880, 4881, 4882, 4883, 4886, 4887, 4888, 4889, 4892, 4893, 4894, 4895, 4898, 4899, 4900, 4901, 4904, 4905, 4906, 4907, 4910, 4911, 4912, 4913, 4916, 4917, 4918, 4919, 4922, 4923, 4924, 4925, 4928, 4929, 4930, 4931, 4934, 4935, 4936, 4937, 4940, 4941, 4942, 4943, 4946, 4947, 4948, 4949, 4952, 4953, 4954, 4955, 4958, 4959, 4960, 4961, 4964, 4965, 4966, 4967, 4970, 4971, 4972, 4973, 4976, 4977, 4978, 4979, 4982, 4983, 4984, 4985, 4988, 4989, 4990, 4991, 4994, 4995, 4996, 4997) {
			@Override
			public int getNextDegradeId(int currentId) {
				return getNextBarrowsId(currentId, getBarrowsCharges(currentId));
			}

			@Override
			public boolean inventoryOnCompletion(int itemId) {
				return getBarrowsCharges(itemId) == 25;
			}
		},

		/**
		 * All pvp armour degrades right after being worn
		 */
		BASE_PVP_ARMOUR(DegradeType.TIMED_DEGRADE, 1, 13858, 13861, 13864, 13867, 13870, 13873, 13876, 13884, 13887, 13890, 13893, 13896, 13899, 13902, 13905, 13908, 13911, 13914, 13917, 13920, 13923, 13926, 13929, 13932, 13935, 13938, 13941, 13944, 13947, 13950) {
			@Override
			public int getNextDegradeId(int currentId) {
				return currentId + 2;
			}
		},

		/**
		 * All degraded pvp armour gives you an hour with them until they degrade completely
		 */
		DEGRADED_PVP_ARMOUR(DegradeType.TIMED_DEGRADE, HOUR, 13860, 13863, 13866, 13869, 13872, 13875, 13878, 13886, 13889, 13892, 13895, 13898, 13901, 13904, 13907, 13910, 13913, 13916, 13919, 13922, 13925, 13928, 13931, 13934, 13937, 13940, 13943, 13946, 13949, 13952) {
			@Override
			public int getNextDegradeId(int currentId) {
				return -1;
			}
		},

		/**
		 * These are the chaotic items that will be degraded. They take 5 hours of combat activity to degrade
		 */
		CHAOTICS(DegradeType.COMBATTED_DEGRADE, HOUR * 5, 18349, 18351, 18353, 18355, 18357) {
			@Override
			public int getNextDegradeId(int currentId) {
				return currentId + 1;
			}

			@Override
			public boolean inventoryOnCompletion(int itemId) {
				return true;
			}
		},

		CRYSTAL_EQUIPMENT(DegradeType.COMBATTED_DEGRADE, (HOUR / 4), 4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223, 4224, 4225, 4226, 4227, 4228, 4229, 4230, 4231, 4232, 4233, 4234) {
			@Override
			public int getNextDegradeId(int currentId) {
				if (currentId == 4223 || currentId == 4234) {
					return -1;
				}
				return currentId + (currentId == 4212 ? 2 : 1);
			}
		},

		/**
		 * All polypore armour (fungal, grifolic, ganodermic) degrades to its next stage after an hour
		 */
		POLYPORE_ARMOUR(DegradeType.COMBATTED_DEGRADE, HOUR, 22458, 22462, 22466, 22470, 22474, 22478, 22482, 22486, 22490) {
			@Override
			public int getNextDegradeId(int currentId) {
				return currentId + 2;
			}
		};

		/**
		 * Finds the id of the next degraded item
		 *
		 * @param currentId
		 * 		The current id of the item
		 */
		public abstract int getNextDegradeId(int currentId);

		/**
		 * The type of degrade that will happen to these degradeables.
		 */
		private final DegradeType type;

		/**
		 * The items that are applicable to this degradeable type
		 */
		private final int[] applicableItems;

		/**
		 * The ticks that must pass for these degradeables to degrade to their next form.
		 */
		private final int degradeTicks;

		DegradeDefinitions(DegradeType type, int degradeTicks, int... applicableItems) {
			this.type = type;
			this.degradeTicks = degradeTicks;
			this.applicableItems = applicableItems;
		}

		/**
		 * Gets the {@code DegradeDefinitions} for the item id, if this is possible.
		 *
		 * @param itemId
		 * 		The item id
		 */
		public static DegradeDefinitions getDegradeDefinition(int itemId) {
			for (DegradeDefinitions degradeDefinitions : DegradeDefinitions.values()) {
				for (int applicables : degradeDefinitions.getApplicableItems()) {
					if (applicables == itemId) {
						return degradeDefinitions;
					}
				}
			}
			return null;
		}

		/**
		 * @return the applicableItems
		 */
		public int[] getApplicableItems() {
			return applicableItems;
		}

		/**
		 * @return the type
		 */
		public DegradeType getType() {
			return type;
		}

		/**
		 * @return the degradeTicks
		 */
		public int getDegradeTicks() {
			return degradeTicks;
		}

		/**
		 * If this is true, we will add the next item to degrade to the player's inventory. Otherwise, it sets the id of
		 * the item in the equipment and refreshes the equipment with the new degraded item
		 *
		 * @param itemId
		 * 		The id of the item
		 */
		public boolean inventoryOnCompletion(int itemId) {
			return false;
		}
	}

	/**
	 * The type of degradation that will happen.
	 *
	 * @author Tyluur
	 */
	public enum DegradeType {
		/**
		 * Items which use this type of degrade type will degrade as time passes and they wield this item.
		 */
		TIMED_DEGRADE {
			@Override
			public String getDegradeInformation() {
				return "while worn";
			}
		},

		/**
		 * Items which use this type of degrade type will degrade as they are used in combat. Every time combat is
		 * processed for the player equipping an item with this degrade type, they get closer to the degrade time.
		 */
		COMBATTED_DEGRADE {
			@Override
			public String getDegradeInformation() {
				return "during combat";
			}
		};

		/**
		 * @return How this degrade type processes degradation
		 */
		public abstract String getDegradeInformation();
	}

	public enum RepairDefinitions {

		AHRIMS(new String[] { "Ahrim's hood", "Ahrim's staff", "Ahrim's robe top", "Ahrim's robe skirt" }, new int[] { 100_000, 75_000, 200_000, 200_000 }, new int[] { 4708, 4710, 4712, 4714 }),
		DHAROKS(new String[] { "Dharok's helm", "Dharok's platebody", "Dharok's platelegs", "Dharok's greataxe" }, new int[] { 200_000, 400_000, 400_000, 250_000 }, new int[] { 4716, 4720, 4722, 4718 }),
		KARILS(new String[] { "Karil's coif", "Karil's crossbow", "Karil's top", "Karil's skirt" }, new int[] { 100_000, 150_000, 150_000, 125_000 }, new int[] { 4732, 4734, 4736, 4738 }),
		VERACS(new String[] { "Verac's helm", "Verac's flail", "Verac's brassard", "Verac's plateskirt" }, new int[] { 200_000, 250_000, 200_000, 150_000 }, new int[] { 4753, 4755, 4757, 4759 }),
		TORAGS(new String[] { "Torag's helm", "Torag's hammers", "Torag's platebody", "Torag's platelegs" }, new int[] { 100_000, 150_000, 150_000, 150_000 }, new int[] { 4745, 4747, 4749, 4751 }),
		GUTHANS(new String[] { "Guthan's helm", "Guthan's warspear", "Guthan's platebody", "Guthan's chainskirt" }, new int[] { 100_000, 200_000, 250_000, 150_000 }, new int[] { 4724, 4726, 4728, 4730 }),
		CHAOTICS(new String[] { "Chaotic rapier", "Chaotic longsword", "Chaotic maul", "Chaotic staff", "Chaotic crossbow", "Chaotic kiteshield" }, new int[] { 5_000_000, 4_000_000, 5_000_000, 2_500_000, 2_500_000, 1_000_000 }, new int[] { 18349, 18351, 18353, 18355, 18357, 18359 });
//		POLYPORES(new String[] { "Fungal visor", "Fungal leggings", "Fungal poncho", "Ganodermic visor", "Ganodermic leggings", "Ganodermic poncho", "Grifolic visor", "Grifolic leggings", "Grifolic poncho" }, new int[] { 100_000, 100_000, 100_000, 350_000, 350_000, 350_000, 200_000, 200_000, 200_000 }, new int[] { });

		/**
		 * The names that are applicable for these repairs
		 */
		private final String[] names;

		/**
		 * The prices that are applicable for these repairs
		 */
		private final int[] prices;

		/**
		 * The array of ids of the fixed items
		 */
		private final int[] repairIds;

		RepairDefinitions(String[] names, int[] prices, int[] repairIds) {
			this.names = names;
			this.prices = prices;
			this.repairIds = repairIds;
		}

		/**
		 * Gets the repair definitions for an item's name
		 *
		 * @param name
		 * 		The name
		 */
		public static RepairDefinitions getDefinitions(String name) {
			for (RepairDefinitions defs : RepairDefinitions.values()) {
				for (String repairName : defs.names) {
					if (name.toLowerCase().contains(repairName.toLowerCase())) {
						return defs;
					}
				}
			}
			return null;
		}

		/**
		 * Finds the index of the name in the {@link #names}
		 *
		 * @param name
		 * 		The name
		 */
		public int indexOf(String name) {
			for (int index = 0; index < names.length; index++) {
				String repairName = names[index];
				if (name.toLowerCase().contains(repairName.toLowerCase())) {
					return index;
				}
			}
			return -1;
		}

		/**
		 * @return the nameKeys
		 */
		public String[] getNames() {
			return names;
		}

		/**
		 * @return the prices
		 */
		public int[] getPrices() {
			return prices;
		}

        public int[] getRepairIds() {
            return this.repairIds;
        }
    }
}
