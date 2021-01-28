package com.runescape.game.content.economy.treasure;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.item.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class TreasureTrailData {

	/**
	 * @return the trailInformation
	 */
	public Map<TreasureTrailTier, String> getTrailInformation() {
		return trailInformation;
	}

	/**
	 * Finds the treasure trail from the {@link #trailInformation} by the tier
	 *
	 * @param tier
	 * 		The tier we want the trail of
	 */
	public String getTrailByTier(TreasureTrailTier tier) {
		if (trailInformation.containsKey(tier)) {
			return trailInformation.get(tier);
		}
		return null;
	}

	/**
	 * Increments the {@link #stepsComplete} array by 1 in the tier index
	 *
	 * @param tier
	 * 		The tier
	 */
	public void incrementSteps(TreasureTrailTier tier) {
		stepsComplete[tier.ordinal()]++;
	}

	/**
	 * @return the stepsComplete
	 */
	public int[] getStepsComplete() {
		return stepsComplete;
	}

	/**
	 * @return the stepsToComplete
	 */
	public int[] getStepsToComplete() {
		return stepsToComplete;
	}

	/**
	 * Adds one to the {@link #trailsComplete} for the tier index
	 *
	 * @param tier
	 * 		The tier
	 */
	public void incrementTrails(TreasureTrailTier tier) {
		trailsComplete[tier.ordinal()]++;
	}

	/**
	 * @return the trailsComplete
	 */
	public int[] getTrailsComplete() {
		return trailsComplete;
	}

	/**
	 * The current trail the user is on
	 */
	private final Map<TreasureTrailTier, String> trailInformation = new HashMap<>();

	/**
	 * The steps the user has complete
	 */
	private final int[] stepsComplete = new int[4];

	/**
	 * The total steps the user must complete
	 */
	private final int[] stepsToComplete = new int[4];

	/**
	 * The amount of trails we have complete
	 */
	private final int[] trailsComplete = new int[4];

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (TreasureTrailTier tier : TreasureTrailTier.values()) {
			System.out.println(tier + ":");
			for (int itemId : tier.rewardItems) {
				ItemDefinitions defs = ItemDefinitions.forId(itemId);
				System.out.println("\t" + defs.getName() + "(" + itemId + "),");
			}
		}
	}

	public enum TreasureTrailTier {
		EASY(2587, 2583, 2585, 2589, 2595, 2591, 2593, 2597, 2633, 2635, 2637, 10366, 7396, 7392, 7388, 7394, 7390, 7386, 7364, 7368, 7362, 7366, 19520, 19522, 19524, 19526, 19528, 10404, 10406, 10424, 10426, 10408, 10410, 10428, 10430, 10412, 10414, 10432, 10434, 10458, 10464, 10470, 10460, 10468, 10444, 10474, 10462, 10466, 10472, 10442) {
			@Override
			public int[] getSteps() {
				return new int[] { 3, 7 };
			}

			@Override
			public double rewardChance() {
				return 40;
			}
		},
		MEDIUM(2605, 2599, 2601, 2603, 2613, 2607, 2609, 2611, 10446, 10448, 10450, 10452, 10454, 10456, 10736, 19380, 19386, 19382, 19390, 19384, 19388, 13107, 13109, 13111, 13113, 13115, 7380, 7372, 7370, 7378, 2577, 2579, 7319, 7321, 7323, 7325, 7327, 19540, 19542, 19544, 19546, 19548) {
			@Override
			public int[] getSteps() {
				return new int[] { 4, 10 };
			}

			@Override
			public double rewardChance() {
				return 30;
			}
		},
		HARD(2627, 2623, 2625, 2629, 2619, 2627, 2623, 2625, 2629, 2619, 2615, 2617, 2621, 2673, 2669, 2671, 2675, 2657, 2653, 2655, 2659, 3486, 3481, 3483, 3485, 3488, 7376, 7384, 7382, 7374, 2581, 8950, 7400, 7399, 7398, 2639, 2641, 2643, 10362, 19281, 19284, 19287, 10470, 10472, 10474, 10440, 10442, 10444, 19374, 19376, 19378, 10368, 10370, 10372, 10374, 10384, 10386, 10388, 10390, 10376, 10378, 10380, 10382) {
			@Override
			public int[] getSteps() {
				return new int[] { 3, 15 };
			}

			@Override
			public double rewardChance() {
				return 20;
			}
		},
		ELITE(19422, 19413, 19416, 19419, 19425, 19308, 19311, 19314, 19317, 19320, 19437, 19428, 19431, 19440, 19407, 19401, 19398, 19404, 19410, 19443, 19445, 19447, 19449, 19451, 19453, 19455, 19457, 19459, 19461, 19463, 19465, 19362, 19364, 19366, 19392, 19394, 19396, 19149, 19146, 19143, 19290, 19293, 19296, 19299, 19302, 19305, 10334, 10342, 10348, 10352, 10346, 10330, 10332, 10336, 10334, 10338, 10340, 10350) {
			@Override
			public int[] getSteps() {
				return new int[] { 10, 20 };
			}

			@Override
			public double rewardChance() {
				return 1.5D;
			}
		};

		TreasureTrailTier(int... rewardItems) {
			this.rewardItems = rewardItems;
		}

		/**
		 * For different tiers of treasure trails, there will be a set amount of steps that must be complete until the
		 * player receives the casket reward. This method finds the lowest amount of steps possible for this tier,
		 * (index 0), and the highest amount of steps possible for this tier (index 1). The amount of steps will be a
		 * random number between the two.
		 */
		public abstract int[] getSteps();

		/**
		 * The chance players have at a reward for this tier
		 */
		public abstract double rewardChance();

		/**
		 * Gets the reward items in an item array
		 */
		public Item[] getRewardItems() {
			List<Item> list = new ArrayList<>();
			for (int rewardItem : rewardItems) {
				list.add(new Item(rewardItem));
			}
			return list.toArray(new Item[list.size()]);
		}

		/**
		 * Dumps all rewards into the {@link #REWARD_IDS} list
		 */
		public static void dumpRewardIds() {
			for (TreasureTrailTier tier : TreasureTrailTier.values()) {
				for (int rewardItem : tier.rewardItems) {
					if (!REWARD_IDS.contains(rewardItem)) {
						REWARD_IDS.add(rewardItem);
					}
				}
			}
		}

		/**
		 * If the item is a treasure trail reward
		 *
		 * @param itemId
		 * 		The id of the item
		 */
		public static boolean isTreasureTrailReward(int itemId) {
			return REWARD_IDS.contains(itemId);
		}

		/**
		 * The items that players can get as a reward from this tier
		 */
		private final int[] rewardItems;

		/**
		 * The list of all ids of items that are received as a reward
		 */
		private static final List<Integer> REWARD_IDS = new ArrayList<>();
	}

	public enum TreasureTrailType {

		/**
		 * Coordinate treasure trails require players to dig on the destination coordinates.
		 */
		COORDINATE,

		/**
		 * Map treasure trails require the players to decrypt the map shown and dig on the destination coordinates
		 */
		MAP,

		/**
		 * Emote treasure trails require the players to perform an emote on the destination coordinates
		 */
		EMOTE,

		/**
		 * Action treasure trails require the players to perform an action on the destination coordinates
		 */
		ACTION
	}

	public enum TrailActionType {
		DIG,
		PERFORM_EMOTE,
		CLICK_OBJECT,
		CLICK_NPC
	}
	
}
