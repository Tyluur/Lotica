package com.runescape.game.world.entity.player;

import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.pvm.times.BossKillTime;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/3/2015
 */
public class BossKillTimeManager {

	/** The list of kill times */
	private final Map<String, BossKillTime> killTimes;

	/** The player */
	private transient Player player;

	public BossKillTimeManager() {
		this.killTimes = new HashMap<>();
	}

	public static String formatTime(long time) {
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
	}

	/**
	 * Displays boss kill times
	 */
	public void displayBossKillTimes() {
		List<String> text = new ArrayList<>();
		for (String name : NPC.BOSS_NAMES) {
			BossKillTime bossKillTime = killTimes.get(name);
			String timeInformation = "N/A";
			if (bossKillTime != null) {
				Long fastest = null;
				for (Long time : bossKillTime.getKillTimes()) {
					if (fastest == null || time < fastest) {
						fastest = time;
					}
				}
				if (fastest == null) {
					continue;
				}
				timeInformation = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(fastest), TimeUnit.MILLISECONDS.toSeconds(fastest) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(fastest)));
			}
			text.add("<col=" + ChatColors.MAROON + ">" + name + "</col>   ->   Best Kill Time: <col=" + ChatColors.MILD_BLUE + ">" + timeInformation);
		}
		Scrollable.sendQuestScroll(player, "Boss Kill Timers", text.toArray(new String[text.size()]));
	}

	/**
	 * Stores the kill time data into the {@code BossKillTime} {@code Object} for the npc
	 *
	 * @param npc
	 * 		The npc that was killed
	 * @param timeTaken
	 * 		The time that was taken to kill the boss
	 */
	public void storeKillTime(NPC npc, long timeTaken) {
		BossKillTime killTime = getBossKillTimeData(npc);
		boolean best = false;
		if (killTime == null) {
			killTime = new BossKillTime(npc.getName());
			best = true;
		} else {
			List<Long> killTimes = killTime.getKillTimes();
			boolean isFastest = true;
			for (Long time : killTimes) {
				if (time < timeTaken) {
					isFastest = false;
					break;
				}
			}
			if (isFastest) {
				best = true;
			}
		}

		killTime.getKillTimes().add(timeTaken);
		killTimes.put(npc.getName(), killTime);

		String timeInformation = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(timeTaken), TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));

		if (best) {
			player.sendMessage("Completion time for <shad=000000><col=" + ChatColors.ORANGE + ">" + npc.getName() + "</shad></col>: <col=" + ChatColors.MAROON + ">" + timeInformation + "</col> - New Personal Record!");
		}
	}

	/**
	 * Gets the {@code BossKillTime} {@code Object} for an npc
	 *
	 * @param npc
	 * 		The npc we're looking for
	 */
	public BossKillTime getBossKillTimeData(NPC npc) {
		return killTimes.get(npc.getName());
	}

	/**
	 * Gets the best time it took to kill the npc
	 *
	 * @param npc
	 * 		The npc
	 */
	public long getBestTime(NPC npc) {
		BossKillTime killTimes = getBossKillTimeData(npc);
		long best = -1;
		for (Long time : killTimes.getKillTimes()) {
			if (best == -1 || time < best) {
				best = time;
			}
		}
		return best;
	}

	/**
	 * Setting the player
	 *
	 * @param player
	 * 		The player
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
}
