package com.runescape.workers.tasks;

public abstract class WorldTask implements Runnable {

	protected boolean needRemove;

	protected int ticksPassed;

	public final void stop() {
		needRemove = true;
	}

	public int getTicksPassed() {
		return ticksPassed;
	}

	public void setTicksPassed(int ticksPassed) {
		this.ticksPassed = ticksPassed;
	}
}
