package com.runescape.game.world.entity.player.quests;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Mar 22, 2014
 */
public class QuestRequirement {

	/**
	 * The text that will be sent for this requirement
	 */
	private final String text;
	
	/**
	 * If the requirement is completed
	 */
	private final boolean completed;

	public QuestRequirement(String text, boolean completed) {
		this.text = text;
		this.completed = completed;
	}

	/**
	 * @return the name
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the requirement
	 */
	public boolean isCompleted() {
		return completed;
	}
}
