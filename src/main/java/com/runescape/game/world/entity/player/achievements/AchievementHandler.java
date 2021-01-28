package com.runescape.game.world.entity.player.achievements;

import com.runescape.game.event.interaction.button.QuestTabInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType.*;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class AchievementHandler {

	/**
	 * All achievement classes must be loaded into the {@link #achievements} map via this method
	 *
	 * @param clearAll
	 * 		If we should clear all the achievements first
	 */
	public static void loadAll(boolean clearAll) {
		if (clearAll) {
			achievements.clear();
		}
		for (String packageName : Utils.getSubDirectories(AchievementHandler.class.getPackage().getName())) {
			AchievementType type = AchievementType.valueOf(packageName.toUpperCase());
			for (Object clazz : Utils.getClassesInDirectory(AchievementHandler.class.getPackage().getName() + "." + packageName)) {
				AbstractAchievement achievement = (AbstractAchievement) clazz;
				achievement.setType(type);
				addAchievement(achievement);
			}
		}
	}

	/**
	 * Adds the achievement to the {@link #achievements} map
	 *
	 * @param achievement
	 * 		The achievement to add
	 */
	private static void addAchievement(AbstractAchievement achievement) {
		AchievementType type = achievement.getType();
		List<AbstractAchievement> achievementList = achievements.get(type);
		if (achievementList == null) {
			achievementList = new ArrayList<>();
		}
		achievementList.add(achievement);
		achievements.put(type, achievementList);
	}

	/**
	 * To select achievement types to view, an interface is sent to the player which displays the possible types of
	 * achievements
	 *
	 * @param player
	 * 		The player
	 */
	public static void displayTypeSelection(Player player) {
		player.getDialogueManager().startDialogue(new SelectAchievementTypeD());
	}

	/**
	 * Gets the achievements of a certain type
	 *
	 * @param type
	 * 		The type
	 */
	public static List<AbstractAchievement> getAchievements(AchievementType type) {
		return achievements.get(type);
	}

	/**
	 * Displays the type of achievements to the player
	 *
	 * @param player
	 * 		The player
	 * @param type
	 * 		The type of achievements
	 */
	public static void displayAchievements(Player player, AchievementType type) {
		if (true) {
			return;
		}
		int interfaceId = 732;
		int componentLength = Utils.getInterfaceDefinitionsComponentsSize(interfaceId);

		// clearing text over the interface
		for (int component = 0; component < componentLength; component++) {
			player.getPackets().sendIComponentText(interfaceId, component, "");
		}

		// sending page numbers over their components
		int[] pages = { 242, 238, 240, 37 };
		for (int pageNumber = 0; pageNumber < pages.length; pageNumber++) {
			player.getPackets().sendIComponentText(interfaceId, pages[pageNumber], "" + (pageNumber + 1));
		}

		// the list of indexes sent, used later
		List<Integer> indexesSent = new ArrayList<>();

		// loop through the achievements
		for (Entry<AchievementType, List<AbstractAchievement>> entry : achievements.entrySet()) {
			AchievementType achievementType = entry.getKey();
			// only display achievements for our type
			if (achievementType != type) {
				continue;
			}
			List<AbstractAchievement> achievementList = entry.getValue();
			for (int index = 0; index < achievementList.size(); index++) {
				AbstractAchievement achievement = achievementList.get(index);
				// the interface data for this index
				Integer[] data = INTERFACE_DATA[index];

				int progress = achievement.getProgress(player);
				player.getPackets().sendHideIComponent(interfaceId, data[0], false);
				player.getPackets().sendItemOnIComponent(interfaceId, data[0], achievement.interfaceItemId(), 1);
				player.getPackets().sendIComponentText(interfaceId, data[1], achievement.title());
				player.getPackets().sendIComponentText(interfaceId, data[2], achievement.description().replaceAll("@TOTAL@", "" + achievement.goal()));
				player.getPackets().sendIComponentText(interfaceId, data[3], (progress == 0 ? "<col=" + ChatColors.RED + ">" : progress < achievement.goal() ? "<col=FFFF00>" : "<col=00FF00>") + "Progress: " + progress + "/" + achievement.goal());
				indexesSent.add(index);
			}
		}

		// hiding all item models for unused items
		for (int i = 0; i < INTERFACE_DATA.length; i++) {
			if (indexesSent.contains(i)) {
				continue;
			}
			player.getPackets().sendHideIComponent(interfaceId, INTERFACE_DATA[i][0], true);
		}

		// the title
		player.getPackets().sendIComponentText(interfaceId, 226, Utils.formatPlayerNameForDisplay(type.name()) + " Achievements");
		if (!player.getInterfaceManager().containsInterface(interfaceId)) {
			player.getInterfaceManager().sendInterface(interfaceId);
		}
	}

	/**
	 * Checks to see if an achievement is finished for a player
	 *
	 * @param player
	 * 		The player
	 * @param clazz
	 * 		The class of the achievement
	 */
	public static boolean isFinished(Player player, Class<?> clazz) {
		AbstractAchievement achievement = getAchievement(clazz);
		if (achievement == null) {
			throw new IllegalStateException("No achievement for class " + clazz);
		}
		return achievement.isFinished(player);
	}

	/**
	 * Forces the completion of an achievement for a player
	 *
	 * @param player
	 * 		The player
	 * @param clazz
	 * 		The class of the achievement
	 */
	public static void forceFinish(Player player, Class<?> clazz) {
		AbstractAchievement achievement = getAchievement(clazz);
		if (achievement == null) {
			throw new IllegalStateException("No achievement for class " + clazz);
		}
		player.getFacade().setProgress(achievement, achievement.goal());
	}

	/**
	 * Gets the progress of an achievement
	 *
	 * @param player
	 * 		The player
	 * @param clazz
	 * 		The class
	 */
	public static int getProgress(Player player, Class<?> clazz) {
		AbstractAchievement achievement = getAchievement(clazz);
		if (achievement == null) {
			throw new IllegalStateException("No achievement for class " + clazz);
		}
		return achievement.getProgress(player);
	}

	/**
	 * Checks if the prerequisites for the achievement have been complete
	 *
	 * @param player
	 * 		The player
	 * @param achievement
	 * 		The achievement
	 */
	private static boolean completedPrerequisites(Player player, AbstractAchievement achievement) {
		Class<?>[] requiredCompleted = achievement.requiredToComplete();
		if (requiredCompleted != null) {
			for (Class<?> clazz : requiredCompleted) {
				AbstractAchievement requiredAchievement = getAchievement(clazz);
				if (requiredAchievement == null) {
					throw new IllegalStateException("No achievement for class " + clazz);
				}
				if (!requiredAchievement.isFinished(player)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Increments the progress of an achievement
	 *
	 * @param player
	 * 		The player
	 * @param clazz
	 * 		The achievement class
	 * @param increment
	 * 		The amount to increment
	 */
	public static void incrementProgress(Player player, Class<?> clazz, int... increment) {
		AbstractAchievement achievement = getAchievement(clazz);
		if (achievement == null) {
			throw new IllegalStateException("No achievement for class " + clazz);
		}
		if (!completedPrerequisites(player, achievement)) {
			return;
		}
		// achievement is complete, no need to do this
		if (achievement.isFinished(player)) {
			return;
		}
		int addedAmount = (increment.length == 1 ? increment[0] : 1);
		player.getFacade().setProgress(achievement, achievement.getProgress(player) + addedAmount);
		int progress = achievement.getProgress(player);
		double progressPercent = ((double) progress / (double) achievement.goal()) * 100;
//		System.out.println("progressPercent=" + progressPercent);
		if (progressPercent >= 100) {
			sendProgressNotification(player, achievement, true);
			achievement.reward(player);
		} else {
			boolean shouldNotify = false;
			double[] checks = { .10, .25, .50, .75, .90 };
			for (double check : checks) {
				double neededForCheck = achievement.goal() * check;
				double ourCheck = achievement.goal() * (progressPercent / 100);

//				System.out.println("check=" + check + "\tneededForCheck=" + neededForCheck + "\tourCheck=" + ourCheck);

				double difference = neededForCheck - ourCheck;
				if (difference < 0) { difference = -difference; }

				if (difference <= 1D) {
					shouldNotify = true;
					break;
				}
			}

			if (shouldNotify) {
				sendProgressNotification(player, achievement, false);
			}
		}
		QuestTabInteractionEvent.refresh(player);
	}

	/**
	 * This method sends the player a notification about the progress of their achievement
	 *
	 * @param player
	 * 		The player
	 * @param achievement
	 * 		The achievement
	 * @param complete
	 * 		If they have completed the achievement
	 */
	public static void sendProgressNotification(Player player, AbstractAchievement achievement, boolean complete) {
		player.sendMessage(complete ? "<col=" + ChatColors.GREEN + ">You have completed this achievement: " + achievement.title() + "." : "<col=" + ChatColors.BLUE + ">Your progress in the achievement \"" + achievement.title() + "\" has been updated (" + achievement.getProgress(player) + "/" + achievement.goal() + ").", true);
		// disabled popup
	/*
		// players dont get the notification for this one
		if (achievement.title().equalsIgnoreCase("Zulrune Lover")) {
			return;
		}

		int interfaceId = 1071;
		StringBuilder bldr = new StringBuilder();
		bldr.append(complete ? "<col=" + ChatColors.GREEN + ">You have completed this achievement: " + achievement.title() + "." : "<col=" + ChatColors.MAROON + ">You have progressed in this achievement: " + achievement.title() + ".");
		bldr.append("<br><br>Progress: " + achievement.getProgress(player) + "/" + achievement.goal());

		player.getPackets().sendItemOnIComponent(interfaceId, 1, achievement.interfaceItemId(), 1);
		player.getPackets().sendIComponentText(interfaceId, 2, achievement.title());
		player.getPackets().sendIComponentText(interfaceId, 3, bldr.toString());

		player.getInterfaceManager().sendOverlay(interfaceId);

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getInterfaceManager().closeOverlay();
				stop();
			}

		}, 10);*/
	}

	/**
	 * Gets a {@code AbstractAchievement} instance from the class
	 *
	 * @param clazz
	 * 		The class
	 */
	public static AbstractAchievement getAchievement(Class<?> clazz) {
		// loop through the achievements
		for (Entry<AchievementType, List<AbstractAchievement>> entry : achievements.entrySet()) {
			List<AbstractAchievement> achievementList = entry.getValue();
			for (AbstractAchievement achievement : achievementList) {
				if (achievement.getClass().equals(clazz)) {
					return achievement;
				}
			}
		}
		return null;
	}

	/**
	 * This method loops through all possible achievements, and checks to see if there is any achievement that is
	 * unfinished.
	 *
	 * @param player
	 * 		The player
	 * @return The achievement that is not finished.
	 */
	public static AbstractAchievement finishedAllAchievements(Player player) {
		for (Entry<AchievementType, List<AbstractAchievement>> entry : achievements.entrySet()) {
			List<AbstractAchievement> achievementList = entry.getValue();
			for (AbstractAchievement achievement : achievementList) {
				if (!achievement.isFinished(player)) {
					return achievement;
				}
			}
		}
		return null;
	}

	/**
	 * This method loops through all achievements and checks if we have completed all the achievements for a certain
	 * type
	 *
	 * @param player
	 * 		The player
	 * @param type
	 * 		The type of achievement
	 */
	public static boolean completedAchievementTypes(Player player, AchievementType type) {
		for (Entry<AchievementType, List<AbstractAchievement>> entry : achievements.entrySet()) {
			List<AbstractAchievement> achievementList = entry.getValue();
			for (AbstractAchievement achievement : achievementList) {
				if (achievement.getType() != type) {
					continue;
				}
				if (!achievement.isFinished(player)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * The data for the interface (1156). [0] = title, [1] = description, [2] = button
	 */
	private static final Integer[][] INTERFACE_DATA = new Integer[][] { { 36, 175, 176, 177, }, { 35, 171, 172, 173, }, { 34, 167, 169, 168 }, { 33, 40, 41, 42, }, { 32, 163, 164, 165, }, { 31, 39, 160, 161, }, { 30, 156, 157, 158, }, { 29, 152, 153, 154, }, { 28, 148, 149, 150, }, { 27, 144, 145, 146, }, { 26, 140, 141, 142, }, { 25, 136, 137, 138, }, { 24, 132, 133, 134, }, { 23, 244, 245, 246, }, { 22, 128, 129, 130, }, { 21, 124, 125, 126, }, { 20, 120, 121, 122, }, { 19, 116, 117, 118, }, { 18, 112, 113, 114, }, { 17, 108, 109, 110, }, { 16, 104, 105, 106, }, { 15, 96, 97, 98, }, { 14, 93, 94, 95, }, { 13, 88, 89, 90, }, { 12, 84, 85, 86, }, { 11, 80, 81, 82, }, { 10, 76, 77, 78, }, { 9, 72, 73, 74, }, { 8, 68, 69, 70, }, { 7, 64, 65, 66, }, { 6, 60, 61, 62, }, { 5, 56, 57, 58, }, { 4, 52, 53, 54, }, { 3, 48, 49, 50, }, { 2, 44, 45, 46, }, };

	/**
	 * The map of all achievements.
	 */
	private static final Map<AchievementType, List<AbstractAchievement>> achievements = new HashMap<>();

	/**
	 * Gets the map of achievements
	 */
	public static Map<AchievementType, List<AbstractAchievement>> getAchievements() {
		return achievements;
	}

	/**
	 * @author Tyluur
	 */
	private static final class SelectAchievementTypeD extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("Select an Achievement Type", "Easy", "Medium", "Hard", "Elite");
		}

		@Override
		public void run(int interfaceId, int option) {
			AchievementType type = null;
			switch (option) {
				case FIRST:
					type = EASY;
					break;
				case SECOND:
					type = MEDIUM;
					break;
				case THIRD:
					type = HARD;
					break;
				case FOURTH:
					type = ELITE;
					break;
			}
			if (type == null) {
				throw new IllegalStateException();
			}
			displayAchievements(player, type);
			end();
		}

		@Override
		public void finish() {
		}
	}
}
