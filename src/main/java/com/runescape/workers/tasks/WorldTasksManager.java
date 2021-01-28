package com.runescape.workers.tasks;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/25/16
 */
public class WorldTasksManager {

	/**
	 * The list of all TASK_INFORMATION_LIST to process
	 */
	private static final CopyOnWriteArrayList<WorldTaskInformation> TASK_INFORMATION_LIST = new CopyOnWriteArrayList<>();

	/**
	 * Processes all tasks
	 */
	public static void processTasks() {
		try {
			for (WorldTaskInformation taskInformation : TASK_INFORMATION_LIST.toArray(new WorldTaskInformation[TASK_INFORMATION_LIST.size()])) {
				if (taskInformation.getContinueCount() > 0) {
					taskInformation.setContinueCount(taskInformation.getContinueCount() - 1);
					continue;
				}
				taskInformation.getTask().setTicksPassed(taskInformation.getTask().getTicksPassed() + 1);
				try {
					taskInformation.getTask().run();
				} catch (Throwable t) {
					System.err.println("Removed the task because of the error thrown:");
					t.printStackTrace();
					TASK_INFORMATION_LIST.remove(taskInformation);
				}
				if (taskInformation.getTask().needRemove) {
					TASK_INFORMATION_LIST.remove(taskInformation);
				} else {
					taskInformation.setContinueCount(taskInformation.getContinueMaxCount());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Schedules a new {@link WorldTask}
	 *
	 * @param task
	 * 		The task to schedule
	 * @param continueCount
	 * 		The amount of ticks to wait before the task is ran
	 * @param continueMaxCount
	 * 		The delay the task will operate at after the first time ran
	 */
	public static void schedule(WorldTask task, int continueCount, int continueMaxCount) {
		if (task == null || continueCount < 0) {
			System.out.println("Couldn't schedule a task, parameters were incorrect!");
			Thread.dumpStack();
			return;
		}
		TASK_INFORMATION_LIST.add(new WorldTaskInformation(task, continueCount, continueMaxCount));
	}

	/**
	 * Schedules a new {@link WorldTask}
	 *
	 * @param task
	 * 		The task to schedule
	 * @param continueCount
	 * 		The amount of ticks to wait before the task is ran
	 */
	public static void schedule(WorldTask task, int continueCount) {
		schedule(task, continueCount, -1);
	}

	/**
	 * Schedules a new {@link WorldTask}
	 *
	 * @param task
	 * 		The task to schedule
	 */
	public static void schedule(WorldTask task) {
		schedule(task, 0, -1);
	}

	@SuppressWarnings("unchecked")
	public static <K> K getTask(Class<?> clazz) {
		for (WorldTaskInformation info : TASK_INFORMATION_LIST) {
			if (info.getTask().getClass().equals(clazz)) {
				return (K) info.getTask();
			}
		}
		return null;
	}
}
