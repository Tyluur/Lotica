package com.runescape.game.world.entity.npc;

public class Drop {

	private int itemId, minAmount, maxAmount;
	private double rate;

	public Drop(int itemId, double rate, int minAmount, int maxAmount) {
		this.itemId = itemId;
		this.rate = rate;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}

	public int getMinAmount() {
		return minAmount;
	}

	public int getExtraAmount() {
		return maxAmount - minAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public int getItemId() {
		return itemId;
	}

	public double getRate() {
		return rate;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public void setMinAmount(int minAmount) {
		this.minAmount = minAmount;
	}

	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	@Override
	public String toString() {
		return "[id=" + itemId + ", rate=" + rate + ", min=" + minAmount + ", max=" + maxAmount + "]";
	}

	/**
	 * The array of possible charms to drop
	 */
	public static final int[] CHARMS = { 12158, 12159, 12160, 12163 };
}
