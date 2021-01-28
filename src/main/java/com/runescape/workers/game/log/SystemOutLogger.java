package com.runescape.workers.game.log;

import com.runescape.utility.logging.ErrLogger;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/5/2016
 */
public class SystemOutLogger implements Runnable {

	/**
	 * The queue of messages to log
	 */
	private static final Queue<String> QUEUED_LOGS = new LinkedBlockingQueue<>();

	/**
	 * The object to synchronize through
	 */
	private static final Object LOCK = new Object();

	/**
	 * Adds the log to the queue
	 *
	 * @param log
	 * 		The log
	 */
	public static void addLogToQueue(String log) {
		synchronized (LOCK) { QUEUED_LOGS.add(log); }
	}

	@Override
	public void run() {
		synchronized (LOCK) {
			flushAll();
		}
	}

	/**
	 * Writes all queued logs to file
	 */
	public static void flushAll() {
		synchronized (LOCK) {
			try {
				String log;
				while ((log = QUEUED_LOGS.poll()) != null) {
					ErrLogger.writeDropboxLog("out/", log, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
