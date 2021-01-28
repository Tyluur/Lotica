package com.runescape.workers.game.log;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 23, 2015
 */
public class GameLog {

	/**
	 * Vital information for the logs.
	 */
	private final String description, username, text;

	public GameLog(String description, String username, String text) {
		this.description = description;
		this.username = username;
		this.text = text;
	}

	public String getDescription() {
		return this.description;
	}

	public String getUsername() {
		return this.username;
	}

	public String getText() {
		return this.text;
	}
}
