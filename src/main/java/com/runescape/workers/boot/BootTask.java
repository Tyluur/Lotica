package com.runescape.workers.boot;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/25/2016
 */
public class BootTask {

	private final Runnable task;

	private int taskNumber;

	public BootTask(Runnable task, int taskNumber) {
		this.task = task;
		this.taskNumber = taskNumber;
	}

    public Runnable getTask() {
        return this.task;
    }

    public int getTaskNumber() {
        return this.taskNumber;
    }

    public void setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber;
    }
}
