package com.runescape.game.content.skills.farming;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.masks.Animation;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public interface FarmingConstants {

	int[] COMPOST_ORGANIC = { 6055, 1942, 1957, 1965, 5986, 5504, 5982, 249, 251, 253, 255, 257, 2998, 259, 261, 263, 3000, 265, 2481, 267, 269, 1951, 753, 2126, 247, 239, 6018 };

	int[] SUPER_COMPOST_ORGANIC = { 2114, 5978, 5980, 5982, 6004, 247, 6469 };

	int REGENERATION_CONSTANT = GameConstants.DEBUG ? 5000 : 120000; // Twelve

	// minutes
	int ALLOTMENT = 0, TREES = 1, HOPS = 2, FLOWERS = 3, FRUIT_TREES = 4, BUSHES = 5, HERBS = 6, COMPOST = 7, MUSHROOMS = 8, BELLADONNA = 9;

	int RAKE = 5341, EMPTY_BUCKET = 1925;

	String[] PATCH_NAMES = { "allotment", "tree", "hops", "flower", "fruit tree", "bush", "herb", "compost", "mushroom", "belladonna" };

	int[][] HARVEST_AMOUNTS = { { 3, 53 }, { 1, 1 }, { 3, 41 }, { 1, 3 }, { 3, 5 }, { 3, 5 }, { 3, 18 }, { 0, 0 }, { 6, 9 }, { 1, 1 } };

	Animation RAKING_ANIMATION = new Animation(2273), WATERING_ANIMATION = new Animation(2293), SEED_DIPPING_ANIMATION = new Animation(2291), SPADE_ANIMATION = new Animation(830), HERB_PICKING_ANIMATION = new Animation(2282), MAGIC_PICKING_ANIMATION = new Animation(2286), CURE_PLANT_ANIMATION = new Animation(2288), CHECK_TREE_ANIMATION = new Animation(832), PRUNING_ANIMATION = new Animation(2275), FLOWER_PICKING_ANIMATION = new Animation(2292), FRUIT_PICKING_ANIMATION = new Animation(2280), COMPOST_ANIMATION = new Animation(2283), BUSH_PICKING_ANIMATION = new Animation(2281), FILL_COMPOST_ANIMATION = new Animation(832);

}
