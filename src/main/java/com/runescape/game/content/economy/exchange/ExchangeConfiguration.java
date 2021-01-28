package com.runescape.game.content.economy.exchange;

/**
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangeConfiguration {

	/**
	 * The interface id of the main interface
	 */
	public static final int MAIN_INTERFACE = 105;

	/**
	 * The interface id of the sell interface
	 */
	public static final int SELL_INTERFACE = 107;

	/**
	 * The interface id of the collection interface
	 */
	public static final int COLLECTION_INTERFACE = 109;

	/**
	 * The array of the buttons used to send the buying interfaces
	 */
	public static final int[] BUY_BUTTON_IDS = { 31, 82, 101, 47, 63, 120 };

	/**
	 * The array of the buttons used to initaliaze the selling process
	 */
	public static final int[] SELL_BUTTON_IDS = { 83, 32, 48, 102, 121, 64 };

	/**
	 * The array of the component ids that are used to display offers in the
	 * collection interface
	 */
	public static final int[] COLLECTION_COMPONENTS = new int[] { 19, 23, 27, 32, 37, 42 };

	public enum Progress {

		BUY_ABORTED(5), 
		SELL_ABORTED(-3),
		RESET(0), 
		BUY_PROGRESSING(4), 
		FINISHED_BUYING(5), 
		SELL_PROGRESSING(11),
		FINISHED_SELLING(13);

		Progress(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		private final int value;
	}

}
