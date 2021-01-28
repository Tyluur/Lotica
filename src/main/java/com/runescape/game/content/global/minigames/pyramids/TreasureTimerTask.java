package com.runescape.game.content.global.minigames.pyramids;

import java.util.TimerTask;

public class TreasureTimerTask extends TimerTask {

	public TreasureTimerTask(PyramidFloor floor) {
		this.floor = floor;
	}

	@Override
	public void run() {
		try {
			if (floor.getFloorPlayers().isEmpty() || floor.getFacade().getTreasurePercentLeft() <= 0) {
				floor.getFacade().setTreasurePercentLeft(0);
				cancel();
				return;
			}
			double reduced = floor.getFacade().getTreasurePercentLeft() - 0.5;
			if (floor.getFacade().getTreasurePercentLeft() - reduced < 0.0D) {
				floor.getFacade().setTreasurePercentLeft(0);
			} else {
				floor.getFacade().setTreasurePercentLeft(reduced);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The pyramid floor this applies to.
	 */
	private final PyramidFloor floor;

}
