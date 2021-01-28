package com.runescape.game.content.global.wilderness.activities;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.wilderness.WildernessActivity;
import com.runescape.game.content.global.wilderness.WildernessBoss;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Dec 31, 2014
 */
public class WildernessBossActivity extends WildernessActivity {

	/**
	 * The time players must wait after a boss has been killed for the next one to spawn
	 */
	public static final long RESPAWN_DELAY = GameConstants.DEBUG ? TimeUnit.SECONDS.toMillis(1) : TimeUnit.MINUTES.toMillis(5);

	@Override
	public String getDescription() {
		if (boss != null && !boss.hasFinished()) {
			return boss.getName() + " spawned in wilderness! Hunt it down!";
		} else {
			return "Wilderness bosses are spawning soon...";
		}
	}

	@Override
	public String getServerAnnouncement() {
		return "Wilderness Bosses are being spawned, pay attention for their locations!";
	}

	@Override
	public void onCreate() {
		nextBossSpawn = System.currentTimeMillis();
	}

	@Override
	public void process() {
		if (shouldSpawnBoss()) {
			List<WildernessSpawns> spawns = new ArrayList<>(Arrays.asList(WildernessSpawns.values()));
			Collections.shuffle(spawns);
			WildernessSpawns spawn = spawns.get(0);
			spawnRandomBoss(spawn);
		}
	}

	/**
	 * Spawns a random boss in the designated tile
	 *
	 * @param spawn
	 * 		The {@code WildernessSpawns} instance we should spawn the boss
	 */
	public void spawnRandomBoss(WildernessSpawns spawn) {
		int npcId = possibleBosses[(Utils.random(possibleBosses.length))];
		boss = new WildernessBoss(npcId, spawn.spawnTile);
		World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Wilderness</col>: A " + boss.getName() + " has spotted near <col=" + ChatColors.RED + ">" + spawn.getDescription().toLowerCase() + "</col>.", false);
	}

	@Override
	public void onFinish() {
		if (boss != null && !boss.hasFinished()) {
			boss.finish();
		}
	}

	@Override
	public long getActivityTime() {
		return TimeUnit.MINUTES.toMillis(30);
	}

	@Override
	public boolean receivesBonus(Player player, Object... params) {
		return true;
	}

	@Override
	public Integer getBonusPoints(Player player) {
		return 5;
	}

	@Override
	public Integer getPointChance(Player player) {
		return 80;
	}

	/**
	 * The next time a boss should be spawned
	 *
	 * @param time
	 * 		The time it should be spawned
	 */
	public void setNextSpawnTime(long time) {
		this.nextBossSpawn = time;
	}

	/**
	 * If we should spawn a boss
	 */
	private boolean shouldSpawnBoss() {
		return !(boss != null && !boss.hasFinished()) && (nextBossSpawn == -1 || (System.currentTimeMillis() >= nextBossSpawn));
	}

	/**
	 * The next boss spawn time
	 */
	private long nextBossSpawn = -1;

	/**
	 * The array of possible bosses
	 */
	private int[] possibleBosses = new int[] { 50, // king black dragon
			6260, // bandos boss
			6203, // zamorak boss
			8133, // corporeal beast
			1158, // kalphite queen
			3334, // wildywyrm
	};

	private WildernessBoss boss;

	private enum WildernessSpawns {

		ROGUES_DEN(new WorldTile(3287, 3914, 0)) {
			@Override
			public String getDescription() {
				return "South of Rogues Den (Lvl 50)";
			}
		},

		RED_DRAGON_ISLE(new WorldTile(3203, 3866, 0)) {
			@Override
			public String getDescription() {
				return "Red Dragon Isle Entrance (Lvl 44)";
			}
		},

		BONEYARD(new WorldTile(3271, 3669, 0)) {
			@Override
			public String getDescription() {
				return "Boneyard (Lvl 19)";
			}
		},

		MAGE_BANK(new WorldTile(3035, 3940, 0)) {
			@Override
			public String getDescription() {
				return "West of Mage Bank (Lvl 53)";
			}
		},

		BANDIT_CAMP(new WorldTile(3035, 3685, 0)) {
			@Override
			public String getDescription() {
				return "Bandit Camp (Lvl 20)";
			}
		};

		WildernessSpawns(WorldTile spawnTile) {
			this.spawnTile = spawnTile;
		}

		/**
		 * The tile
		 */
		private final WorldTile spawnTile;

		/**
		 * The description of the location
		 */
		public abstract String getDescription();
	}

}