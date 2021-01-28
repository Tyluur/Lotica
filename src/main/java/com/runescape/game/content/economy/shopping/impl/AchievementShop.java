package com.runescape.game.content.economy.shopping.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class AchievementShop extends DefaultShop {

	@Override
	public boolean canBuyItem(Player player, Item item) {
		UnlockData data = UnlockData.getDataForItem(item.getId());
		if (data == null) {
			System.err.println("Unable to find UnlockData for item:\t" + item);
			player.sendMessage("You can't buy this item");
			return false;
		}
		for (AchievementType type : data.types) {
			if (!AchievementHandler.completedAchievementTypes(player, type)) {
				player.sendMessage("You must complete all " + type.name().toLowerCase() + " achievements to buy this item.");
				return false;
			}
		}
		return true;
	}

	@Override
	public Integer getBuyPrice(Item item) {
		int[] info = getItemInformation(item);
		if (info == null) {
			return Integer.MAX_VALUE;
		}
		return info[info.length - 1];
	}

	@Override
	public Integer getSellPrice(Item item) {
		int[] info = getItemInformation(item);
		if (info == null) {
			return Integer.MAX_VALUE;
		}
		return info[info.length - 1];
	}

	@Override
	protected int getItemAmount(int itemId) {
		if (itemId == 995) {
			return 5_000_000;
		} else {
			return 1;
		}
	}

	/**
	 * Finds item information from the {@link #ITEMS} array
	 *
	 * @param item
	 * 		The item
	 */
	private int[] getItemInformation(Item item) {
		for (int[] ITEM : ITEMS) {
			for (int aITEM : ITEM) {
				if (aITEM == item.getId()) {
					return ITEM;
				}
			}
		}
		return null;
	}

	/**
	 * An array of the item and prices in this shop
	 */
	private static final int[][] ITEMS = new int[][] {
															 // easy achievement diary items
			                                                 { 14631, 11756, 14577, 14571, 13560, 11136, 15345, 8650, 1_500_000 },
															 // medium achievement diary items
															 {  14662, 11757, 14578, 14572, 13561, 11138, 15347, 8654, 3_500_000 },
															 // hard achievement diary items
															 { 14663, 11758, 14579, 14573, 13562, 11140, 15349, 8666, 5_000_000 },
															 // elite achievement diary items
															 {  19763, 19757, 19749, 19766, 19760, 19754, 19748, 15241, 10_000_000 },
															 // agile armour
															 { 14936, 14938, 500_000 },
															 // rogue armour
															 { 5554, 5553, 5555, 5556, 5557, 1_250_000 },
															 // lumberjack armour
															 { 10941, 10939, 10940, 10933, 2_000_000 },
															 // witchdoctor
															 { 20046, 20044, 20045, 2_000_000 },
															 // golden mining armour
															 { 20789, 20791, 20790, 20787, 20788, 2_500_000 },
															 // cooking gauntlets
															 { 775, 500_000 },
															 // black ibis
															 { 21482, 21480, 21481, 21483, 1_500_000 },
															 // fishing armour
															 { 24427, 24428, 24429, 24430, 1_000_000 }

	};

	private enum UnlockData {

		EASY_ACHIEVEMENTS_DIARY_ITEMS(AchievementType.EASY, 14631, 11756, 14577, 14571, 13560, 11136, 15345, 8650),
		MEDIUM_ACHIEVEMENTS_DIARY_ITEMS(AchievementType.MEDIUM, 14662, 11757, 14578, 14572, 13561, 11138, 15347, 8654),
		HARD_ACHIEVEMENTS_DIARY_ITEMS(AchievementType.HARD, 14663, 11758, 14579, 14573, 13562, 11140, 15349, 8666),
		ELITE_ACHIEVEMENTS_DIARY_ITEMS(AchievementType.ELITE, 19763, 19757, 19749, 19766, 19760, 19754, 19748, 15241),
		AGILE_ARMOUR(AchievementType.EASY, 14936, 14938),
		ROGUE_ARMOUR(AchievementType.MEDIUM, 5554, 5553, 5555, 5556, 5557),
		LUMBERJACK_ARMOUR(AchievementType.EASY, 10941, 10939, 10940, 10933),
		WITCHDOCTOR_ARMOUR(AchievementType.ELITE, 20046, 20044, 20045),
		GOLDEN_MINING_ARMOUR(AchievementType.HARD, 20789, 20791, 20790, 20787, 20788),
		COOKING_GAUNTLETS(new AchievementType[] { AchievementType.EASY, AchievementType.MEDIUM }, 775),
		BLACK_IBIS_ARMOUR(AchievementType.ELITE, 21482, 21480, 21481, 21483),
		FISHING_ARMOUR(AchievementType.MEDIUM, 21482, 21480, 21481, 21483, 24427, 24428, 24429, 24430);

		UnlockData(AchievementType type, int... itemIds) {
			this.types = new AchievementType[] { type };
			this.itemIds = itemIds;
		}

		UnlockData(AchievementType[] types, int... itemIds) {
			this.types = types;
			this.itemIds = itemIds;
		}

		/** The type of achievement applicable */
		private final AchievementType[] types;

		/** The item ids */
		private final int[] itemIds;

		/**
		 * Finds data for the item
		 *
		 * @param itemId
		 * 		The item
		 */
		public static UnlockData getDataForItem(int itemId) {
			for (UnlockData data : UnlockData.values()) {
				for (int itemIds : data.itemIds) {
					if (itemIds == itemId) {
						return data;
					}
				}
			}
			return null;
		}
	}
}
