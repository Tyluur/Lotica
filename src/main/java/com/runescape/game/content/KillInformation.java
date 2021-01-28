package com.runescape.game.content;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/20/2015
 */
public class KillInformation {

	/**
	 * The address of the player killed
	 */
	private final String macAddress;

	/**
	 * The time the player was killed
	 */
	private final long killTime;

	public KillInformation(String macAddress, long killTime) {
		this.macAddress = macAddress;
		this.killTime = killTime;
	}

	/**
	 * Gets the mac address of the player we killed
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Gets the time the player was killed
	 */
	public long getKillTime() {
		return killTime;
	}
}
