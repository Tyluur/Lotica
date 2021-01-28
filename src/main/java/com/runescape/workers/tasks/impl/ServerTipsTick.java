package com.runescape.workers.tasks.impl;

import com.runescape.game.world.World;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/15/2015
 */
public class ServerTipsTick extends WorldTask {

	/** The time between messages */
	private static final long TIME_BETWEEN_MESSAGE = TimeUnit.MINUTES.toMillis(30);

	/** The tips that players can see */
	private static final String[] SERVER_TIPS = new String[] {
			"Thieving is a great way to make quick money.",
			"You can make fast money by completing achievements.",
			"Quickly gear up with your presets list! '?' -> My account",
			"The wizard at home will teleport you all around the world.",
			"You can buy/sell items with offline players in the grand exchange." };

	/** The last time a tip was sent */
	private long lastTimeMessageSent;

	@Override
	public void run() {
		if (World.getPlayers().size() > 0 && (lastTimeMessageSent == -1 || (System.currentTimeMillis() - lastTimeMessageSent > TIME_BETWEEN_MESSAGE))) {
			World.sendWorldMessage("<img=6><col=" + ChatColors.PURPLE + ">Quick Tip</col>: " + Utils.randomArraySlot(SERVER_TIPS), false);
			lastTimeMessageSent = System.currentTimeMillis();
		}
	}

}
