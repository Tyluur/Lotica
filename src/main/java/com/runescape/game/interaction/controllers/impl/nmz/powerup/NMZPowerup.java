package com.runescape.game.interaction.controllers.impl.nmz.powerup;

import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public abstract class NMZPowerup {

	/**
	 * The name of the powerup
	 */
	public abstract String name();

	/**
	 * The message players receive when they activate the powerup
	 */
	public abstract String activationMessage();

	/**
	 * The message players receive when the powerup has been depleted
	 */
	public abstract String depletionMessage();

	/**
	 * The key in players' attributes for this powerup
	 */
	public abstract String powerupKey();

	/**
	 * The object id of the powerup
	 */
	public abstract int getObjectId();

	/**
	 * The time this powerup is effective for, set in milliseconds
	 */
	public abstract long timeEffective();

	/**
	 * This method is what the powerup will do when it is picked up by a player
	 *
	 * @param player
	 * 		The player picking up the powerup
	 */
	public void onPickup(Player player) {
		player.putAttribute(powerupKey(), System.currentTimeMillis());
	}

	/**
	 * Gets the time the powerup was activated
	 *
	 * @param player
	 * 		The player
	 */
	public long getActivatedTime(Player player) {
		return player.getAttribute(powerupKey(), 0L);
	}

	/**
	 * What happens to the player when the powerup has been depleted
	 *
	 * @param player
	 * 		The  player
	 */
	public void onDeplete(Player player) {

	}
}
