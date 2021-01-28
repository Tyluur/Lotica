package com.runescape.game.content.skills;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class DXPAlgorithms {

	/**
	 * The amount of minutes that double experience is enabled for when the player uses a lamp
	 */
	public static final int MINUTES_FOR_DXP = 60;

	/**
	 * Gets the amount of bonus experience the player should receive
	 *
	 * @param player
	 * 		The player
	 */
	public static double getBonusExperience(Player player) {
		long startTime = GameConstants.DOUBLE_EXPERIENCE_TIMES[0];
		Long duration = player.getFacade().getTimeSpentSinceDate(startTime, player.getSignInTime());
		long seconds = duration / 1000;
		double hours = Utils.round(((double) seconds / (double) 3600), 1);
		double top = hours - 10;
		double bottom = 7.5;
		double divided = (top / bottom);
		double pow = Math.pow(divided, 2);
		double bonus = Utils.round(pow, 1) + 1.1;
		if (hours >= 10) {
			bonus = 1.1;
		}
//		System.out.println("hours=" + hours + ", bonus=" + bonus);
		return bonus;
	}

	/**
	 * Checks if double experience is currently on
	 */
	public static boolean isDoubleExperienceOn() {
		return System.currentTimeMillis() < GameConstants.DOUBLE_EXPERIENCE_TIMES[1];
	}

	/**
	 * Checks to see if the player should receive double experience
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean enabledHourlyBonus(Player player) {
		return player.getFacade().getDoubleExperienceOverAt() != -1 && !(System.currentTimeMillis() >= player.getFacade().getDoubleExperienceOverAt());
	}

}
