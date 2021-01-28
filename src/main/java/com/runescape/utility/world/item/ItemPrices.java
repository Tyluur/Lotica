package com.runescape.utility.world.item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public enum ItemPrices {

	BRONZE_LG(11814, 352),
	BRONZE_SK(11816, 352),
	IRON_LG(11818, 1232),
	IRON_SK(11820, 1232),
	STEEL_LG(11822, 4400),
	STEEL_SK(11824, 4400),
	BLACK_LG(11826, 12_000),
	BLACK_SK(11828, 12_000),
	INITIATE_SET(9668, 25_000),
	MITHRIL_LG(11830, 11440),
	MITHRIL_SK(11832, 11440),
	ADAMANT_LG(11834, 32000),
	ADAMANT_SK(11836, 32000),
	PROSELYTE_LG(9666, 30000),
	PROSELYTE_SK(9670, 30000),
	RUNE_LG(11838, 218600),
	RUNE_SK(11840, 218600),
	GREEN_DHIDE(11864, 14200),
	BLUE_DHIDE_SET(11866, 16680),
	RED_DHIDE_SET(11868, 20010),
	BLACK_DHIDE_SET(11870, 26_000),
	SPLITBARK(11876, 105000),
	BLUE_MYSTIC_SET(11872, 235000),
	LIGHT_MYSTIC_SET(11960, 235000),
	DARK_MYSTIC_SET(11962, 235000),
	PHOENIXNECK(11090, 35000),
	STANDARD_HERBS(new int[] { 199, 201, 203, 205, 207, 209, 211, 203 }, new int[] { 200, 200, 200, 200, 1_000, 200, 200, 200, }),
	MORE_EXPENSIVE_HERBS(new int[] { 3051, 251, 2485, 217, 219 }, 1500),
	TRAINING_SWORD(9703, 100_000),
	GUTHIX_BODY(10378, 60_000),
	SARADOMIN_BODY(10386, 82_500),
	ZAMORAK_BODY(10370, 100_000);

	private final int[] itemIds;

	private final int[] prices;

	ItemPrices(int itemId, int price) {
		this.itemIds = new int[] { itemId };
		this.prices = new int[] { price };
	}

	ItemPrices(int[] itemIds, int price) {
		this.itemIds = itemIds;
		this.prices = new int[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
			prices[i] = price;
		}
	}

	ItemPrices(int[] itemIds, int[] prices) {
		this.itemIds = itemIds;
		this.prices = prices;
	}

	/**
	 * @return the itemIds
	 */
	public int[] getItemIds() {
		return itemIds;
	}

	/**
	 * @return the prices
	 */
	public int[] getPrice() {
		return prices;
	}

	/**
	 * Gets the modified price of the item
	 *
	 * @param itemId
	 * 		The item
	 */
	public static int getModifiedPrice(int itemId) {
		for (ItemPrices prices : ItemPrices.values()) {
			for (int i = 0; i < prices.itemIds.length; i++) {
				if (prices.itemIds[i] == itemId) {
					return prices.prices[i];
				}
			}
		}
		return -1;
	}

}
