package com.runescape.game.content.global.wilderness;

import com.runescape.Main;
import com.runescape.game.GameConstants;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Dec 31, 2014
 */
public class WildernessActivityManager implements Runnable {

	/**
	 * Getting the instance of this class
	 */
	public static WildernessActivityManager getSingleton() {
		return SINGLETON;
	}

	/**
	 * The current activity that is happening
	 */
	private WildernessActivity currentActivity;

	/**
	 * The next time an activity will be executed
	 */
	private long nextActivityTime;

	/**
	 * The delay that happens in between wilderness activities
	 */
	public static final long ACTIVITY_INBETWEEN_DELAY = GameConstants.DEBUG ? TimeUnit.SECONDS.toMillis(1440) : TimeUnit.MINUTES.toMillis(120);

	/**
	 * The message players will see when the wilderness activity is over
	 */
	public static final String ACTIVITY_COMPLETE_MESSAGE = "<img=6><col=" + ChatColors.MAROON + ">Wilderness</col>: The current wilderness activity has ended! Please wait for the next one.";

	/**
	 * The list of wilderness activities in the server
	 */
	private List<WildernessActivity> wildernessActivities = new ArrayList<>();

	/**
	 * The list of wilderness activities performed
	 */
	private List<WildernessActivity> activitiesPerformed = new ArrayList<>();

	/**
	 * The instance of this class
	 */
	private static final WildernessActivityManager SINGLETON = new WildernessActivityManager();

	/**
	 * When called, this method will register all wilderness activities into the server
	 */
	public void load() {
		Utils.getClassesInDirectory(WildernessActivityManager.class.getPackage().getName() + ".activities").forEach((clazz) -> {
			WildernessActivity activity = (WildernessActivity) clazz;
			if (activity.getActivityTime() == -1) {
				System.out.println("Not adding activity " + activity.getClass().getSimpleName());
				return;
			}
			wildernessActivities.add(activity);
		});
		nextActivityTime = GameConstants.DEBUG ? System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5) : System.currentTimeMillis() + (ACTIVITY_INBETWEEN_DELAY / 2);
		CoresManager.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			if (Main.get().getStartTime() == -1 || World.getPlayers().size() == 0) {
				return;
			}
			if (System.currentTimeMillis() >= nextActivityTime) {
				/**
				 * If we have an activity currently happening, we will remove it
				 */
				if (currentActivity != null) {
					currentActivity.onFinish();
					currentActivity = null;
					nextActivityTime = System.currentTimeMillis() + ACTIVITY_INBETWEEN_DELAY;
					/** Sending the server announcement */
					World.sendWorldMessage(ACTIVITY_COMPLETE_MESSAGE, false);
					return;
				}
				WildernessActivity randomActivity = getRandomActivity();

				randomActivity.onCreate();
				randomActivity.setActivityInitializeTime(System.currentTimeMillis());

				nextActivityTime = System.currentTimeMillis() + ACTIVITY_INBETWEEN_DELAY;
				currentActivity = randomActivity;

				/** Sending the server announcement */
				// to disable sending server announcementes
//				World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Wilderness</col>: " + randomActivity.getServerAnnouncement(), false);
			} else if (currentActivity != null) {
				long activityTime = currentActivity.getActivityTime();
				currentActivity.process();
				if (activityTime == 0) {
					return;
				}
				long timeOver = currentActivity.getActivityInitializeTime() + activityTime;
				/**
				 * Each activity has a time period it can last for but it can't
				 * be the amount of time in {@link #ACTIVITY_INBETWEEN_DELAY}.
				 * If the current activity has been happening for this amount of
				 * time, it will finish and the next activity will happen in
				 * {@link #ACTIVITY_INBETWEEN_DELAY} ms
				 */
				if (System.currentTimeMillis() >= timeOver) {
					currentActivity.onFinish();
					currentActivity = null;
					nextActivityTime = System.currentTimeMillis() + ACTIVITY_INBETWEEN_DELAY;
					/** Sending the server announcement */
					World.sendWorldMessage(ACTIVITY_COMPLETE_MESSAGE, false);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If the wilderness activity is the current activity
	 *
	 * @param activity
	 * 		The activity to check for
	 */
	public boolean isActivityCurrent(WildernessActivity activity) {
		if (activity == null) {
			return false;
			//throw new RuntimeException("Checking if activity is current activity but it was null!");
		}
		return currentActivity != null && currentActivity.getClass().getSimpleName().equals(activity.getClass().getSimpleName());
	}

	/**
	 * If the wilderness activity is the current activity
	 *
	 * @param clazz
	 * 		The activity to check for
	 */
	public boolean isActivityCurrent(Class<? extends WildernessActivity> clazz) {
		WildernessActivity activity = getWildernessActivity(clazz);
		if (activity == null) {
			return false;
			//throw new RuntimeException("Checking if activity is current activity but it was null!");
		}
		return currentActivity != null && currentActivity.getClass().getSimpleName().equals(activity.getClass().getSimpleName());
	}

	/**
	 * Gets a wilderness activity by the class
	 *
	 * @param clazz
	 * 		The class
	 */
	@SuppressWarnings("unchecked")
	public <T extends WildernessActivity> T getWildernessActivity(Class<? extends WildernessActivity> clazz) {
		for (WildernessActivity activity : wildernessActivities) {
			if (activity.getClass().getSimpleName().equals(clazz.getSimpleName())) {
				return (T) activity;
			}
		}
		return null;
	}

	/**
	 * Gives the player their bonus points for engaging in the {@link #currentActivity} wilderness activity
	 *
	 * @param player
	 * 		The player
	 * @param params
	 * 		The parameters for the activity
	 */
	public void giveBonusPoints(Player player, Object... params) {
		if (currentActivity != null) {
			if (currentActivity.receivesBonus(player, params)) {
				currentActivity.giveBonusPoints(player);
			}
		}
	}

	/**
	 * Gets a random activity from the {@link #wildernessActivities} list
	 *
	 * @return A {@code WildernessActivity} {@code Object}
	 */
	private WildernessActivity getRandomActivity() {
		if (activitiesPerformed.size() == wildernessActivities.size()) {
			activitiesPerformed.clear();
		}
		List<WildernessActivity> activities = new ArrayList<>(wildernessActivities);
		Collections.shuffle(activities);
		WildernessActivity random = activities.get(0);
		while (activitiesPerformed.contains(random)) {
			Collections.shuffle(activities);
			random = activities.get(0);
		}
		activitiesPerformed.add(random);
		return random;
	}

	/**
	 * If we have an activity currently running, a description of the activity is necessary. This method finds that
	 * description
	 */
	public String getActivityDescription() {
		if (currentActivity == null) {
			return null;
		}
		try {
			return currentActivity.getDescription();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}