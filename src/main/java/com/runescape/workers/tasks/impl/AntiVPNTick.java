package com.runescape.workers.tasks.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/7/2016
 */
public class AntiVPNTick extends WorldTask {
	
	/**
	 * The key that separates entries
	 */
	private static final String SEPARATOR = "~~~";

	/**
	 * The delay between pruning entries
	 */
	private static final long PRUNE_DELAY = TimeUnit.HOURS.toMillis(1);
	
	/**
	 * The list of entries
	 */
	private static final List<String> ENTRIES = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The object that operations synchronize through
	 */
	private static final Object LOCK = new Object();

	/**
	 * The maximum amount of votes per hour
	 */
	private static final int MAX_HOURLY_VOTES = 30;
	
	@Override
	public void run() {
		synchronized (LOCK) {
			for (Iterator<String> iterator = ENTRIES.iterator(); iterator.hasNext(); ) {
				String entry = iterator.next();
				long time = Long.parseLong(entry.split(SEPARATOR)[0]);
				if (Utils.timeHasPassed(time, PRUNE_DELAY)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Submits a vote
	 *
	 * @param player
	 * 		The player
	 */
	public static void submitVote(Player player) {
		synchronized (LOCK) {
			ENTRIES.add(System.currentTimeMillis() + SEPARATOR + player.getUsername() + SEPARATOR + player.getMacAddress() + SEPARATOR);
			System.out.println(player.getUsername() + " has submitted another vote (#" + getVoteAmount(player) + ")");
		}
	}

	/**
	 * Checks if the player is legible to vote
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean legibleToVote(Player player) {
		int voteCount = getVoteAmount(player);
		return voteCount < MAX_HOURLY_VOTES;
	}

	/**
	 * Gets the amount of votes the player has done
	 *
	 * @param player
	 * 		The player
	 */
	private static int getVoteAmount(Player player) {
		synchronized (LOCK) {
			int amount = 0;
			for (String entry : ENTRIES) {
				String[] dataSplit = entry.split(SEPARATOR);
				String name = dataSplit[1].trim();
				String mac = dataSplit[2].trim();
				if (name.equalsIgnoreCase(player.getUsername()) || mac.equalsIgnoreCase(player.getMacAddress())) {
					amount++;
				}
			}
			return amount;
		}
	}

}
