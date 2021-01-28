package com.runescape.game.content;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.loyalty.LoyaltyRewards;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 10/21/15
 */
public class LoyaltyManager {

	/**
	 * The amount of minutes that pass between the checks in {@link #process(Player)}
	 */
	private static final int MINUTES_TO_RECEIVE_POINTS = 10;

	/**
	 * The amount of points the player receives every time they increment
	 */
	private static final int POINTS_PER_INCREMENT = 100;

	/**
	 * The amount of {@link #activityPoints} we must have to possibly have a loyalty point increase in the {@link
	 * #process(Player)} method
	 */
	private static final int ACTIVITY_POINT_CHECK = 1000;

	/**
	 * The maximum chance attempts to receive loyalty points
	 */
	private static final int MAX_CHANCE_ATTEMPTS = 3;

	/**
	 * The amount of loyalty points we have
	 */
	private int points;

	/**
	 * The activity "chance" that is incremented every time a logic packet has been processed
	 */
	private int activityPoints;

	/**
	 * The attempts we've had at the 25% chance
	 */
	private int chanceAttempts;

	/**
	 * The last time we received loyalty points
	 */
	private long lastChanceCheckTime;

	/**
	 * The list of rewards we have purchased, used for reclaiming.
	 */
	private List<LoyaltyRewards> purchasedRewards = new ArrayList<>();

	/**
	 * Processing loyalty point management
	 */
	public void process(Player player) {
		if (activityPoints >= ACTIVITY_POINT_CHECK) {
			if (lastTimeReceivedPointsLapsed()) {
				if (Utils.percentageChance(25) || chanceAttempts >= MAX_CHANCE_ATTEMPTS) {
					player.sendMessage("<col=" + ChatColors.MAROON + ">You receive " + POINTS_PER_INCREMENT + " loyalty points for your activity on " + GameConstants.SERVER_NAME + ".</col>");
					lastChanceCheckTime = System.currentTimeMillis();
					incrementPoints(POINTS_PER_INCREMENT);
					activityPoints = 0;
					chanceAttempts = 0;
				} else {
					lastChanceCheckTime = System.currentTimeMillis();
					chanceAttempts++;
				}
			}
		}
	}

	/**
	 * This checks to see if we should receive points if the time between the last time we received points has lapsed
	 */
	public boolean lastTimeReceivedPointsLapsed() {
		return lastChanceCheckTime == -1 || TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastChanceCheckTime) >= MINUTES_TO_RECEIVE_POINTS;
	}

	/**
	 * Increments the amount of points we have
	 *
	 * @param amount
	 * 		The amount of points we have
	 */
	public void incrementPoints(int amount) {
		this.points += amount;
	}

	/**
	 * Sets the amount of points we have
	 *
	 * @param points
	 * 		The amount of points to set
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * Gets the amount of points we have
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Adds a new reward to the list of rewards we have purchased
	 *
	 * @param loyaltyReward
	 * 		The reward
	 */
	public boolean addPurchasedReward(LoyaltyRewards loyaltyReward) {
		return purchasedRewards.add(loyaltyReward);
	}

	/**
	 * Finding out if we have purchased this reward before
	 *
	 * @param loyaltyReward
	 * 		The reward
	 */
	public boolean purchasedReward(LoyaltyRewards loyaltyReward) {
		return purchasedRewards.contains(loyaltyReward);
	}

	/**
	 * Increments the loyalty activity points
	 */
	public void incrementActivityPoints() {
		activityPoints++;
	}
}
