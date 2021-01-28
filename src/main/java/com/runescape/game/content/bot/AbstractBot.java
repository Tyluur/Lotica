package com.runescape.game.content.bot;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/18/2016
 */
public abstract class AbstractBot {

	/**
	 * The index of the bot, used for {@link #hoverTiles()}
	 */
	private int index;

	/**
	 * The bot instance
	 */
	protected Player bot;

	/**
	 * The last time we've pulsed the bot and completed an action in the {@link #onPulse()} method
	 */
	private long lastTimePulsed;

	/**
	 * The suggested names for this bot
	 */
	public abstract String[] botNames();

	/**
	 * The amount of bots. The location of the bot in {@link #hoverTiles()} is based on the index of this bot
	 */
	public abstract Integer getBotAmounts();

	/**
	 * The tiles the bot will hover around
	 *
	 * @return A {@code WorldTile} {@code Object}
	 */
	public abstract WorldTile[] hoverTiles();

	/**
	 * The amount of time (in milliseconds) inbetween pulses in activity
	 */
	public abstract long activityTime();

	/**
	 * What happens when the bot pulses
	 */
	public abstract void onPulse();

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getLastTimePulsed() {
		return lastTimePulsed;
	}

	public void setLastTimePulsed(long lastTimePulsed) {
		this.lastTimePulsed = lastTimePulsed;
	}

	public boolean shouldPulse() {
		return lastTimePulsed == -1 || (System.currentTimeMillis() - lastTimePulsed > activityTime());
	}

	public Player getBot() {
		return bot;
	}

	public void setBot(Player bot) {
		this.bot = bot;
	}

	public WorldTile getHoverTile() {
		return hoverTiles()[index];
	}

	public String getIdentifier() {
		return index + "-" + getClass().getName();
	}
}
