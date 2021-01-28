package com.runescape.workers.tasks;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/29/2016
 */
public class WorldTaskInformation {

	private WorldTask task;

	private int continueCount;

	private int continueMaxCount;

	public WorldTaskInformation(WorldTask task, int continueCount, int continueMaxCount) {
		setTask(task);
		setContinueCount(continueCount);
		setContinueMaxCount(continueMaxCount);
		if (continueMaxCount == -1) {
			task.needRemove = true;
		}
	}

	public WorldTask getTask() {
		return this.task;
	}

	public int getContinueCount() {
		return this.continueCount;
	}

	public int getContinueMaxCount() {
		return this.continueMaxCount;
	}

	public void setTask(WorldTask task) {
		this.task = task;
	}

	public void setContinueCount(int continueCount) {
		this.continueCount = continueCount;
	}

	public void setContinueMaxCount(int continueMaxCount) {
		this.continueMaxCount = continueMaxCount;
	}
}