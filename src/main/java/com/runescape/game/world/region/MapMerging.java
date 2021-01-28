package com.runescape.game.world.region;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public enum MapMerging {
	DUEL_ARENA_TO_EDGEVILLE {
		@Override
		public void merge() {
			int topFromX = 419, toRegionX = 380;
			int tomFromY = 408, toRegionY = 439;
			int ratio = 4;

			RegionBuilder.copyAllPlanesMap(topFromX, tomFromY, toRegionX, toRegionY, ratio); // top
																								// left
			RegionBuilder.copyAllPlanesMap(topFromX - 4, tomFromY, toRegionX - 4, toRegionY, ratio); // top
																										// right

			RegionBuilder.copyAllPlanesMap(topFromX - 4, tomFromY - 4, toRegionX - 4, toRegionY - 4, ratio); // bottom
																												// left
			RegionBuilder.copyAllPlanesMap(topFromX, tomFromY - 4, toRegionX, toRegionY - 4, ratio); // bottom
																										// right
		}
	};

	/**
	 * Merges the maps
	 */
	public abstract void merge();

	/**
	 * Merges all the maps
	 */
	public static void start() {
		for (MapMerging map : MapMerging.values()) {
			map.merge();
		}
	}
}
