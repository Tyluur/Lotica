package com.runescape.game.content.skills.farming;

import com.runescape.game.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public enum ProductInfo implements FarmingConstants {

	/**
	 * Allotments
	 */
	Potato(5318, 1, 1942, 0, 8, 9, 10, ALLOTMENT),
	Onion(5319, 5, 1957, 1, 9.5, 10.5, 10, ALLOTMENT),
	Cabbage(5324, 7, 1965, 2, 10, 11.5, 10, ALLOTMENT),
	Tomato(5322, 12, 1982, 3, 12.5, 14, 10, ALLOTMENT),
	Sweetcorn(5320, 20, 5986, 4, 17, 19, 10, 6, ALLOTMENT),
	Strawberry(5323, 31, 5504, 5, 26, 29, 10, 6, 2, ALLOTMENT),
	Watermelon(5321, 47, 5982, 6, 48.5, 54.5, 10, 8, 4, ALLOTMENT),

	/**
	 * Herbs
	 */
	Guam(5291, 9, 199, 0, 11, 12.5, 20, HERBS),
	Marrentill(5292, 14, 201, 1, 13.5, 15, 20, HERBS),
	Tarromin(5293, 19, 203, 2, 16, 18, 20, HERBS),
	Harralander(5294, 26, 205, 3, 21.5, 24, 20, HERBS),
	Rannar(5295, 32, 207, 4, 27, 30.5, 20, HERBS),
	Toadflax(5296, 38, 3049, 5, 34, 38.5, 20, HERBS),
	Irit(5297, 44, 209, 6, 43, 48.5, 20, HERBS),
	Avantoe(5298, 50, 211, 7, 54.4, 61.5, 20, HERBS),
	Kwuarm(5299, 56, 213, 6, 69, 78, 20, HERBS),
	Snapdragon(5300, 62, 3051, 6, 87.5, 98.5, 20, HERBS),
	Cadantine(5301, 67, 215, 6, 106.5, 120, 20, HERBS),
	Lantadyme(5302, 73, 2485, 6, 134.5, 151.5, 20, HERBS),
	Dwarf(5303, 79, 217, 6, 170.5, 192, 20, HERBS),
	Torstol(5304, 85, 219, 6, 199.5, 224.5, 20, HERBS),
	Fellstalk(21621, 91, 21626, 6, 225, 315.6, 20, HERBS),
	Wergali(14870, 46, 213, 8, 52.8, 52.8, 20, HERBS),
	Gout(6311, 65, 3261, 27, 105, 45, 20, HERBS),

	/**
	 * Flowers
	 */
	Marigold(5096, 2, 6010, 0, 8.5, 47, 5, FLOWERS),
	Rosemary(5097, 11, 6014, 1, 12, 66.5, 5, FLOWERS),
	Nasturtium(5098, 24, 6012, 2, 19.5, 111, 5, FLOWERS),
	Woad(5099, 25, 1793, 3, 20.5, 115.5, 5, FLOWERS),
	Limpwurt(5100, 26, 225, 4, 21.5, 120, 5, FLOWERS),
	White_lily(14589, 52, 14583, 6, 70, 250, 20, 4, -1, FLOWERS),

	/**
	 * Hops
	 */
	Barley(5305, 3, 6006, 9, 8.5, 9.5, 10, 4, 1, HOPS),
	Hammerstone(5307, 4, 5994, 0, 9, 10, 10, 4, 1, HOPS),
	Asgarnian(5308, 8, 5996, 1, 10.9, 12, 10, 5, 3, HOPS),
	Jute(5306, 13, 5931, 10, 13, 14.5, 10, 5, 3, HOPS),
	Yanillian(5309, 16, 5998, 3, 14.5, 16, 10, 6, 1, HOPS),
	Krandorian(5310, 21, 6000, 5, 17.5, 19.5, 10, 7, HOPS),
	Wildbood(5311, 28, 6002, 7, 23, 26, 10, 7, 1, HOPS),

	/**
	 * Trees
	 */
	Oak(5370, 15, 6043, 1, 467.3, 14, 40, TREES),
	Willow(5371, 30, 6045, 6, 1456.5, 25, 40, 6, TREES),
	Maple(5372, 45, 6047, 17, 3403.4, 45, 40, 8, TREES),
	Yew(5373, 60, 6049, 26, 7069.9, 81, 40, 10, TREES),
	Magic(5374, 75, 6051, 41, 13768.3, 145.5, 40, 12, TREES),

	/**
	 * Trees of the fruits :)
	 */
	Apple(5496, 27, 1955, 1, 1199.5, 22, 160, 6, FRUIT_TREES),
	Banana(5497, 33, 1963, 26, 1841.5, 28, 160, 6, FRUIT_TREES),
	Orange(5498, 39, 2108, 65, 2470.2, 35.5, 160, 6, FRUIT_TREES),
	Curry(5499, 42, 5970, 90, 2906.9, 40, 160, 6, FRUIT_TREES),
	Pineapple(5500, 51, 2114, 129, 4605.7, 57, 160, 6, FRUIT_TREES),
	Papaya(5501, 57, 5972, 154, 6146.4, 72, 160, 6, FRUIT_TREES),
	Palm(5502, 68, 5974, 193, 10150.1, 110.5, 160, 6, FRUIT_TREES),

	/**
	 * Bushes of the bush
	 */
	Redberry(5101, 10, 1951, -4, 64, 11.5, 20, 5, BUSHES),
	Cadavaberry(5102, 22, 753, 6, 102.5, 18, 20, 6, BUSHES),
	Dwellberry(5103, 36, 2126, 19, 177.5, 31.5, 20, 7, BUSHES),
	Jangerberry(5104, 48, 247, 31, 284.5, 50.5, 20, 8, BUSHES),
	Whiteberry(5105, 59, 239, 42, 437.5, 78, 20, 8, BUSHES),
	Poison_ivy(5106, 70, 6018, 188, 675, 120, 20, 8, BUSHES),

	Compost_Bin(7836, 1, -1, 0, 8, 14, 2, 15, COMPOST),

	Bittercap(17825, 53, 17821, 0, 61.5, 57.7, 40, 6, 0, MUSHROOMS),
	Morchella(21620, 74, 21622, 1, 22, 77.7, 25, 6, 0, MUSHROOMS),

	Belladonna(5281, 63, 2398, 0, 91, 512, 80, BELLADONNA);

	private static Map<Short, ProductInfo> products = new HashMap<>();

	static {
		for (ProductInfo product : ProductInfo.values()) {
			products.put((short) product.seedId, product);
		}
	}

	private int seedId;

	private int level;

	private int productId;

	private int configIndex;

	private int type;

	private int maxStage;

	private int stageSkip;

	private double experience, plantingExperience;

	private int cycleTime;

	ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int type) {
		this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, maxStage, 0, type);
	}

	ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int stageSkip, int type) {
		this.seedId = seedId;
		this.level = level;
		this.productId = productId;
		this.configIndex = configIndex;
		this.plantingExperience = plantingExperience;
		this.experience = experience;
		this.cycleTime = cycleTime;
		this.maxStage = maxStage;
		this.stageSkip = stageSkip;
		this.type = type;
	}

	ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int type) {
		this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, 4, 0, type);
	}

	public static ProductInfo getProduct(int itemId) {
		return products.get((short) itemId);
	}

	public static boolean isProduct(Item item) {
		for (ProductInfo info : ProductInfo.values()) {
			if (info.productId == item.getId()) {
				return true;
			}
		}
		return false;
	}

    public int getSeedId() {
        return this.seedId;
    }

    public int getLevel() {
        return this.level;
    }

    public int getProductId() {
        return this.productId;
    }

    public int getConfigIndex() {
        return this.configIndex;
    }

    public int getType() {
        return this.type;
    }

    public int getMaxStage() {
        return this.maxStage;
    }

    public int getStageSkip() {
        return this.stageSkip;
    }

    public double getExperience() {
        return this.experience;
    }

    public double getPlantingExperience() {
        return this.plantingExperience;
    }

    public int getCycleTime() {
        return this.cycleTime;
    }
}

