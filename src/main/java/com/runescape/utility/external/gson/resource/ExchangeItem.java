package com.runescape.utility.external.gson.resource;

import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangeItem {

	/**
	 * The amount of minutes that guide prices are updated at
	 */
	public static final long UPDATE_TIME = 60;

	/**
	 * The id of the exchange item
	 */
	private final int itemId;
	
	/**
	 * The list of prices it has gone for
	 */
	private List<Integer> prices = new ArrayList<>();

	/**
	 * The last time the price was changed
	 */
	private long lastTimePriceChanged = -1;

	/**
	 * The guide price
	 */
	private int guidePrice;

	@Override
	public String toString() {
		return "ExchangeItem[id=" + itemId + ", price=" + prices + "]";
	}
	
	public ExchangeItem(int itemId) {
		this.itemId = itemId;
		this.prices = new ArrayList<>();
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}
	
	/**
	 * Calculates the guide prices by updating them every
	 */
	public boolean calculateGuidePrices() {
//		System.out.println(this + "\t\tguidePrice=" + guidePrice + ", lastTimePriceChanged=" + lastTimePriceChanged + "");
		if (guidePrice <= 0 || lastTimePriceChanged == -1 || Utils.timeHasPassed(lastTimePriceChanged, UPDATE_TIME)) {
			Integer[] numArray = getPrices().toArray(new Integer[getPrices().size()]);
			Arrays.sort(numArray);
			long median;
			if (numArray.length % 2 == 0) {
				median = (long) (((double) numArray[numArray.length / 2] + (double) numArray[numArray.length / 2 - 1]) / 2);
			} else {
				median = (long) numArray[numArray.length / 2];
			}
			lastTimePriceChanged = System.currentTimeMillis();
			guidePrice = median > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) median;
//			System.out.println("Set guide price for " + getItemId() + " to " + guidePrice);
			return true;
		}
		return false;
	}

	/**
	 * @return the prices
	 */
	public List<Integer> getPrices() {
		if (prices == null) {
			prices = new ArrayList<>();
		}
		return prices;
	}

    public long getLastTimePriceChanged() {
        return this.lastTimePriceChanged;
    }

    public int getGuidePrice() {
        return this.guidePrice;
    }

    public void setLastTimePriceChanged(long lastTimePriceChanged) {
        this.lastTimePriceChanged = lastTimePriceChanged;
    }
}
