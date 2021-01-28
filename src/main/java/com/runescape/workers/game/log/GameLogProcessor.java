package com.runescape.workers.game.log;

import com.runescape.game.GameConstants;
import com.runescape.utility.world.DateCalculation;
import com.runescape.workers.game.core.CoresManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 23, 2015
 */
public class GameLogProcessor implements Runnable {

	/**
	 * The directory the logs will be stored in
	 */
	private static final String DIRECTORY = GameConstants.FILES_PATH + "players/logs/";

	/**
	 * How the date will be formatted in the file
	 */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * The lock object
	 */
	private static final Object LOCK = new Object();

	/**
	 * The queue of logs to add to the player's log file
	 */
	private final Queue<GameLog> queue = new LinkedBlockingDeque<>();

	@Override
	public void run() {
		synchronized (LOCK) {
			try {
				GameLog log;
				while ((log = queue.poll()) != null) {
					Date date = new Date();
					try (FileWriter fw = new FileWriter(getFile(log), true)) {
						fw.write(DATE_FORMAT.format(date) + " - [" + log.getDescription().toUpperCase() + "]\t" + log.getText() + "\r\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a file for the player's log
	 *
	 * @param log
	 * 		The log
	 */
	protected File getFile(GameLog log) {
		File file = new File(getDirectory() + log.getUsername() + ".txt");
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * @return the directory
	 */
	protected String getDirectory() {
		return DIRECTORY + DateCalculation.getMonthName() + "/Week_" + DateCalculation.getWeekNumber() + "/";
	}

	/**
	 * Submits the log staticly to the queue
	 *
	 * @param log
	 * 		The log
	 */
	public static void submitLog(GameLog log) {
		CoresManager.LOG_PROCESSOR.appendLog(log);
	}

	/**
	 * Adds a log to the list
	 *
	 * @param log
	 * 		The log
	 */
	public void appendLog(GameLog log) {
		synchronized (LOCK) {
			queue.add(log);
		}
	}

}
