package com.runescape.game.world.entity.player.pvm.times;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/3/2015
 */
public class BossKillTime {

	/** The name of the boss */
	private final String bossName;

	/** The list of time it took to kill the bosses */
	private final List<Long> killTimes;

	public BossKillTime(String bossName) {
		this.bossName = bossName;
		this.killTimes = new ArrayList<>();
	}

	public String getBossName() {
		return bossName;
	}

	public List<Long> getKillTimes() {
		return killTimes;
	}
}
