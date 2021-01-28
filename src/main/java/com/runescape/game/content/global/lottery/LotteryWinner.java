package com.runescape.game.content.global.lottery;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class LotteryWinner {

	public LotteryWinner(String name, int cashWon) {
		this.name = name;
		this.cashWon = cashWon;
	}

	/**
	 * Gets the name of the winner
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the amount of cash won
	 * @return
	 */
	public int getCashWon() {
		return cashWon;
	}

	/**
	 * The name of the winner
	 */
	private final String name;

	/**
	 * The amount of cash the winner won
	 */
	private final int cashWon;
}
