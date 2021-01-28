package com.runescape.game.content;

import com.runescape.game.GameConstants;
import com.runescape.game.world.World;
import com.runescape.utility.Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/15/2015
 */
public class GlobalPlayers {

	/** The file registrations are stored in */
	private static final String PLAYER_REGISTRATION_FILE = GameConstants.HOSTED ? GameConstants.LINUX_HOST ? "/root/gamedata/newplayers/%DATE%.txt" : "C:/gamedata/players/newplayers/%DATE%.txt" : "./data/players/newplayers/%DATE%.txt";
	private static final String ONLINE_TODAY_FILE = GameConstants.HOSTED ? GameConstants.LINUX_HOST ? "/root/gamedata/dailyplayers/%DATE%.txt" : "C:/gamedata/players/dailyplayers/%DATE%.txt" : "./data/players/dailyplayers/%DATE%.txt";
	private static int[] maxOnlineToday = new int[2];

	public static void main(String[] args) {
		loadOnlineToday();
	}

	public static void loadOnlineToday() {
		try {
			String fileLocation = ONLINE_TODAY_FILE.replaceAll("%DATE%", DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate());
			File file = new File(fileLocation);
			if (!file.exists()) {
				makeFile(file);
			}
			int[] amountInFile = new int[2];
			String line = Utils.getText(fileLocation);
			if (line.length() > 0) {
				String[] split = line.split("-");
				//ex:   3-5 [3 being real, 5 being fake]
				for (int i = 0; i < amountInFile.length; i++) {
					amountInFile[i] = Integer.parseInt(split[i].trim());
				}
			}
			maxOnlineToday = amountInFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void makeFile(File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
	}

	private static boolean newDayPassed() {
		String fileLocation = ONLINE_TODAY_FILE.replaceAll("%DATE%", DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate());
		File file = new File(fileLocation);
		return !file.exists();
	}

	public static void refreshOnlineToday() {
		int size = World.getPlayers().size();
		if (newDayPassed() || (size > maxOnlineToday[0] || Utils.getFakePlayerCount() > maxOnlineToday[1])) {
			dumpOnlineToday();
			loadOnlineToday();
			System.out.println("New maximum for players online: " + Arrays.toString(maxOnlineToday));
		}
	}

	private static void dumpOnlineToday() {
		String fileLocation = ONLINE_TODAY_FILE.replaceAll("%DATE%", DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate());
		int size = World.getPlayers().size();
		String text = size + "-" + Utils.getFakePlayerCount();
		Utils.writeTextToFile(fileLocation, text, false);
	}

	/**
	 * Updates the new players text file with the amount of players in the day.
	 */
	@SuppressWarnings("deprecation")
	public static void addRegistrationToFile(String username) {
		try {
			String fileLocation = PLAYER_REGISTRATION_FILE.replaceAll("%DATE%", DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate());
			File file = new File(fileLocation);
			if (!file.exists()) {
				makeFile(file);
			}
			List<String> lines = Utils.getFileText(fileLocation);
			List<String> newText = new ArrayList<>();
			newText.add("Amount of new players: " + lines.size());
			for (int i = 0; i < lines.size(); i++) {
				if (i == 0) {
					continue;
				}
				String line = lines.get(i);
				if (line.length() > 0) {
					newText.add(line);
				}
			}
			newText.add(username);
			String text = "";
			for (String line : newText) {
				text += line + "\n";
			}
			text = text.trim();
			Utils.writeTextToFile(fileLocation, text, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
