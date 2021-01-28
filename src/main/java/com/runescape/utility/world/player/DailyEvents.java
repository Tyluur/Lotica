package com.runescape.utility.world.player;

import com.runescape.game.GameConstants;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/18/2015
 */
public class DailyEvents {

	/**
	 * The format for the way it will appear
	 */
	private static final DateFormat FORMATTER = new SimpleDateFormat("HH:mm");

	/**
	 * The array of invalid skills
	 */
	private static final int[] INVALID_SKILLS = new int[] { Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGE, Skills.PRAYER, Skills.MAGIC, Skills.HITPOINTS, Skills.DUNGEONEERING, Skills.FARMING, Skills.CONSTRUCTION };

	/**
	 * The directory of the file that has daily bonus skill information in
	 */
	private static final String DAILY_SKILL_INFORMATION_FILE = GameConstants.FILES_PATH + "daily_skill_bonus.txt";

	/**
	 * The last time the daily skill was set
	 */
	private static long lastTimeSet = -1;

	/**
	 * The skill of the day
	 */
	private static int skillOfTheDay = -1;

	/**
	 * Processes all daily events
	 */
	public static void processDailyTasks() {
		checkDailySkills();
	}

	/**
	 * Checks the {@link #DAILY_SKILL_INFORMATION_FILE} file and reads the amount of time left for the daily skill. If
	 * the daily skill was set more than a day ago, it is set to a new skill.
	 */
	private static void checkDailySkills() {
		if (getDailySkillFile().exists()) {
			String text = Utils.getText(DAILY_SKILL_INFORMATION_FILE).trim();
			String[] split = text.split("-");
			if (!text.equalsIgnoreCase("")) {
				long timeSet = Long.parseLong(split[1].trim());
				lastTimeSet = timeSet;
				if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timeSet) >= 24) {
					setSkill();
				}
				skillOfTheDay = getDailySkill();
			} else {
				setSkill();
			}
		} else {
			setSkill();
		}
	}

	private static File getDailySkillFile() {
		return new File(DAILY_SKILL_INFORMATION_FILE);
	}

	/**
	 * This method sets the daily skill
	 */
	private static void setSkill() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(getDailySkillFile()))) {
			int skillId = Utils.random(0, Skills.SKILL_NAME.length - 1);
			while (isSkillInvalid(skillId)) {
				skillId = Utils.random(0, Skills.SKILL_NAME.length - 1);
			}
			skillOfTheDay = skillId;
			writer.write(skillId + "-" + (lastTimeSet = System.currentTimeMillis()));
			writer.newLine();
			writer.flush();
			World.sendWorldMessage("<col=" + ChatColors.RED + "><img=6>SOTD</col>: The new skill of the day is " + Skills.SKILL_NAME[skillId].toLowerCase() + "! Train this for bonus experience.", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finding the skill of the day from the file
	 *
	 * @return A {@code Integer} {@code Object}
	 */
	public static int getDailySkill() {
		try {
			String text = Utils.getText(DAILY_SKILL_INFORMATION_FILE);
			String[] split = text.split("-");
			return skillOfTheDay = Integer.parseInt(split[0]);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * If the skill is invalid to set, based on the skill ids contained in {@link #INVALID_SKILLS}
	 */
	private static boolean isSkillInvalid(int skillId) {
		for (int invalidSkill : INVALID_SKILLS) {
			if (invalidSkill == skillId) {
				return true;
			}
		}
		return false;
	}

	public static String getTimeTillNext() {
		if (lastTimeSet == -1) {
			return "00:00";
		} else {
			long next = TimeUnit.HOURS.toMillis(24) + lastTimeSet;
			long difference = System.currentTimeMillis() - next;
			return FORMATTER.format(new Date(difference));
		}
	}

	/**
	 * Gets the stored skill of the day value: not from the file.
	 */
	public static int getSkillOfTheDay() {
		return skillOfTheDay;
	}
}
