package com.runescape.utility.external.gson.resource;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class ItemInformation {

	/**
	 * The examine text for the item
	 */
	private String examine;
	
	/**
	 * The bonuses of the item
	 */
	private int[] bonuses;

	/**
	 * The real price of the item from the grand exchange in 2012
	 */
	private int realPrice;

	public ItemInformation() {
		this.setExamine(null);
		this.setBonuses(null);
	}

	public ItemInformation(String examine) {
		this.setExamine(examine);
		this.setBonuses(null);
	}

	public ItemInformation(String examine, int[] bonuses) {
		this.setExamine(examine);
		this.setBonuses(bonuses);
	}

	public String getExamine() {
		return this.examine;
	}

	public int[] getBonuses() {
		return this.bonuses;
	}

	public int getRealPrice() {
		return this.realPrice;
	}

	public void setExamine(String examine) {
		this.examine = examine;
	}

	public void setBonuses(int[] bonuses) {
		this.bonuses = bonuses;
	}

	public void setRealPrice(int realPrice) {
		this.realPrice = realPrice;
	}
}
