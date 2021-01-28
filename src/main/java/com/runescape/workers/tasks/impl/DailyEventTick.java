package com.runescape.workers.tasks.impl;

import com.runescape.game.content.global.lottery.Lottery;
import com.runescape.utility.world.player.DailyEvents;
import com.runescape.workers.tasks.WorldTask;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/18/2015
 */
public class DailyEventTick extends WorldTask {

	@Override
	public void run() {
		DailyEvents.processDailyTasks();
		Lottery.process();
	}
}
