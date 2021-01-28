package com.runescape.game.content.global.wilderness;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Dec 31, 2014
 */
public abstract class WildernessActivity {

	/**
	 * The description of the wilderness activity. This description is sent to the player's information tab
	 *
	 * @return A {@code String} {@code Object}
	 */
	public abstract String getDescription();

	/**
	 * The announcement that will display throughout the server when the wilderness activity has been created
	 *
	 * @return A {@code String} {@code Object}
	 */
	public abstract String getServerAnnouncement();

	/**
	 * What the wilderness activity will perform when it is created
	 */
	public abstract void onCreate();

	/**
	 * If the wilderness activity overridse this method, it will have its own processing system
	 */
	public abstract void process();

	/**
	 * What the wilderness activity should do when it's time is up
	 */
	public abstract void onFinish();

	/**
	 * The time that the activity was started at
	 */
	private long activityInitializeTime;

	/**
	 * The amount of time the activity exists for
	 *
	 * @return A {@code Long} {@code Object}
	 */
	public abstract long getActivityTime();

	/**
	 * If the player receives any bonus because of their activity in this wilderness activity. If they do receive a
	 * bonus, they will also receive {@link #getBonusPoints()} wilderness points if they're lucky. Their chance is based
	 * on {@link #getPointChance()}
	 *
	 * @param player
	 * 		The player
	 * @param params
	 * 		The parameters
	 */
	public abstract boolean receivesBonus(Player player, Object... params);

	/**
	 * The amount of bonus wilderness points to give the player if they are lucky enough. Chance of reward is {@link
	 * #getPointChance()}
	 */
	public abstract Integer getBonusPoints(Player player);

	/**
	 * The chance the player has to receive a reward
	 *
	 * @return The chance
	 */
	public abstract Integer getPointChance(Player player);

	/**
	 * Gives the player their bonus points for activity in the wilderness
	 *
	 * @param player
	 * 		The player
	 */
	public void giveBonusPoints(Player player) {
		if (getBonusPoints(player) == 0) {
			return;
		}
		if (Utils.percentageChance(getPointChance(player))) {
			int amount = getBonusPoints(player);
			player.getInventory().addItem(Wilderness.WILDERNESS_TOKEN, amount);
			player.sendMessage("<col=" + ChatColors.MAROON + ">You receive " + amount + " wilderness points as a reward for your activity in the wilderness.");
			if (player.getAttributes().get("wilderness_points_information_message") == null) {
				player.sendMessage("<col=" + ChatColors.MAROON + ">Check your information tab to see how many points you now have.");
				player.getAttributes().put("wilderness_points_information_message", true);
			}
		}
	}

	/**
	 * @return the activityInitializeTime
	 */
	public long getActivityInitializeTime() {
		return activityInitializeTime;
	}

	/**
	 * @param activityInitializeTime
	 * 		the activityInitializeTime to set
	 */
	public void setActivityInitializeTime(long activityInitializeTime) {
		this.activityInitializeTime = activityInitializeTime;
	}

}
