package com.runescape.workers.game.core;

import com.alex.store.Index;
import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.economy.exchange.ExchangeWorker;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.OwnedObjectManager;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.elite.Lotica_Lover;
import com.runescape.game.world.region.Region;
import com.runescape.utility.SerializableFilesManager;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.utility.world.player.DTRank;
import com.runescape.utility.world.player.PkRank;
import com.runescape.workers.game.BackupGenerator;
import com.runescape.workers.game.DecoderThreadFactory;
import com.runescape.workers.game.LoticaThreadFactory;
import com.runescape.workers.game.SlowThreadFactory;
import com.runescape.workers.game.log.GameLogProcessor;
import com.runescape.workers.game.log.SystemOutLogger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CoresManager {

	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	public static final int SERVER_WORKERS_COUNT = AVAILABLE_PROCESSORS >= 6 ? AVAILABLE_PROCESSORS - (AVAILABLE_PROCESSORS >= 12 ? 7 : 5) : AVAILABLE_PROCESSORS;

	public static final ScheduledExecutorService SLOW_EXECUTOR = Executors.newScheduledThreadPool(SERVER_WORKERS_COUNT, new SlowThreadFactory());

	public static final ScheduledExecutorService DATABASE_WORKER = Executors.newSingleThreadScheduledExecutor(new LoticaThreadFactory("DatabaseWorker"));

	public static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor();

	public static final GameUpdateWorker gameUpdateWorker = new GameUpdateWorker();

	public static final ExecutorService serverWorkerChannelExecutor = AVAILABLE_PROCESSORS >= 6 ? Executors.newFixedThreadPool(AVAILABLE_PROCESSORS - (AVAILABLE_PROCESSORS >= 12 ? 7 : 5), new DecoderThreadFactory()) : Executors.newSingleThreadExecutor(new DecoderThreadFactory());

	public static final ExecutorService serverBossChannelExecutor = Executors.newSingleThreadExecutor(new DecoderThreadFactory());

	public static final Timer FAST_EXECUTOR = new Timer("Fast Executor");

	public static final GameLogProcessor LOG_PROCESSOR = new GameLogProcessor();

	public static volatile boolean shutdown;

	private CoresManager() {

	}

	public static void init() {
		gameUpdateWorker.start();
		ExchangeWorker.get().loadUp();
		scheduleAtFixedRate(LOG_PROCESSOR, 1, 1, TimeUnit.SECONDS);
		scheduleAtFixedRate(new SystemOutLogger(), 1, 1, TimeUnit.MINUTES);
	}

	/**
	 * Registering all tasks that are processed in the workers.
	 */
	public static void registerTasks() {
		addRestoreRunEnergyTask();
		addRestoreHitPointsTask();
		addRestoreSkillsTask();
		addRestoreSpecialAttackTask();
		addSummoningEffectTask();
		addOwnedObjectsTask();
		addAccountsSavingTask();
		addCleanMemoryTask();
		addBackupProcessor();
		addPlaytimeIncrementationTask();
	}

	private static void addRestoreRunEnergyTask() {
		FAST_EXECUTOR.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning()) { continue; }
						if (player.getNextRunDirection() == -1 && player.getRunEnergy() < 100) {
							double toIncrease = (((double) (8 + (player.getSkills().getLevel(Skills.AGILITY) / 6)) / (double) 100) * 2);
							player.setRunEnergy(player.getRunEnergy() + toIncrease);
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 0, 1000);
	}

	private static void addRestoreHitPointsTask() {
		FAST_EXECUTOR.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning()) {
							continue;
						}
						player.restoreHitPoints();
					}
					for (NPC npc : World.getNPCs()) {
						if (npc == null || npc.isDead() || npc.hasFinished()) {
							continue;
						}
						npc.restoreHitPoints();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 0, 6000);
	}

	private static void addRestoreSkillsTask() {
		FAST_EXECUTOR.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player == null || !player.isRunning()) {
							continue;
						}
						int ammountTimes = player.getPrayer().usingPrayer(0, 8) ? 2 : 1;
						if (player.isResting()) {
							ammountTimes += 1;
						}
						boolean berserker = player.getPrayer().usingPrayer(1, 5);
						for (int skill = 0; skill < 25; skill++) {
							if (skill == Skills.SUMMONING) {
								continue;
							}
							for (int time = 0; time < ammountTimes; time++) {
								int currentLevel = player.getSkills().getLevel(skill);
								int normalLevel = player.getSkills().getLevelForXp(skill);
								if (currentLevel > normalLevel) {
									if (skill == Skills.ATTACK || skill == Skills.STRENGTH || skill == Skills.DEFENCE || skill == Skills.RANGE || skill == Skills.MAGIC) {
										if (berserker && Utils.getRandom(100) <= 15) {
											continue;
										}
									}
									player.getSkills().setLevel(skill, currentLevel - 1);
								} else if (currentLevel < normalLevel) {
									player.getSkills().setLevel(skill, currentLevel + 1);
								} else {
									break;
								}
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 0, 60_000);
	}

	private static void addRestoreSpecialAttackTask() {
		FAST_EXECUTOR.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning()) {
							continue;
						}
						player.getCombatDefinitions().restoreSpecialAttack();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 0, 30000);
	}

	private static void addSummoningEffectTask() {
		scheduleWithFixedDelay(() -> {
			try {
				for (Player player : World.getPlayers()) {
					if (player == null || player.getFamiliar() == null || player.isDead() || !player.hasFinished()) {
						continue;
					}
					if (player.getFamiliar().getOriginalId() == 6814) {
						player.heal(20);
						player.setNextGraphics(new Graphics(1507));
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, 0, 15, TimeUnit.SECONDS);
	}

	private static void scheduleWithFixedDelay(Runnable o, int i, int i1, TimeUnit seconds) {
		SLOW_EXECUTOR.scheduleWithFixedDelay(o, i, i1, seconds);
	}

	private static void addOwnedObjectsTask() {
		CoresManager.scheduleWithFixedDelay(() -> {
			try {
				OwnedObjectManager.processAll();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private static void addAccountsSavingTask() {
		CoresManager.scheduleWithFixedDelay(() -> {
			try {
				saveFiles(false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, 15, 15, TimeUnit.MINUTES);
	}

	private static void addCleanMemoryTask() {
		CoresManager.scheduleWithFixedDelay(() -> {
			try {
				cleanMemory(Runtime.getRuntime().freeMemory() < GameConstants.MIN_FREE_MEM_ALLOWED);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	private static void addBackupProcessor() {
		scheduleAtFixedRate(new BackupGenerator(), 1, 1, TimeUnit.MINUTES);
	}

	private static void addPlaytimeIncrementationTask() {
		scheduleAtFixedRate(() -> World.players().forEach(p -> {
			try {
				Long lastLogicPacketTime = p.getAttribute("last_logic_packet_time");
				if (lastLogicPacketTime == null) {
					return;
				}
				// if player afks for 2mins+ they get no playtime
				if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastLogicPacketTime) > 120) {
					return;
				}
				p.getFacade().addSecondsSpentOnline();
				if (p.getFacade().getSecondsSpentOnline() % 60 == 0) {
					AchievementHandler.incrementProgress(p, Lotica_Lover.class, 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}), 1, 1, TimeUnit.SECONDS);
	}

	public static void saveFiles(boolean shutdown) {
		try {
			if (shutdown) {
				World.players().forEach(Player::realFinish);
			} else {
				World.players().filter(p -> p.getControllerManager().getController() != null).forEach(SerializableFilesManager::savePlayer);
			}
			PkRank.save();
			DTRank.save();
			GsonStartup.getClass(ExchangeItemLoader.class).save();
			GsonStartup.getClass(ExchangePriceLoader.class).save();

			System.out.println("All files saved in thread " + Thread.currentThread().getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cleanMemory(boolean force) {
		if (force) {
			ItemDefinitions.clearItemsDefinitions();
			NPCDefinitions.clearNPCDefinitions();
			ObjectDefinitions.clearObjectDefinitions();
			World.getRegions().values().forEach(Region::removeMapFromMemory);
		}
		Index[] indexes = Cache.STORE.getIndexes();
		for (Index index : indexes) {
			if (index == null) {
				continue;
			}
			index.resetCachedFiles();
		}
		CoresManager.FAST_EXECUTOR.purge();
		System.gc();
	}

	public static void shutdown() {
		serverWorkerChannelExecutor.shutdown();
		serverBossChannelExecutor.shutdown();
		FAST_EXECUTOR.cancel();
		SLOW_EXECUTOR.shutdown();
		shutdown = true;
	}

	public static void schedule(Runnable runnable, long delay, TimeUnit unit) {
		try {
			SLOW_EXECUTOR.schedule(runnable, delay, unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.err.println("#schedule#");
//		Thread.dumpStack();
	}

	public static void scheduleAtFixedRate(Runnable runnable, int initialDelay, int period, TimeUnit unit) {
		try {
			SLOW_EXECUTOR.scheduleAtFixedRate(runnable, initialDelay, period, unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.err.println("#scheduleAtFixedRate#");
//		Thread.dumpStack();
	}

	public static void submit(Runnable runnable) {
		try {
			SLOW_EXECUTOR.submit(runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.err.println("#submit#");
//		Thread.dumpStack();
	}

	public static void execute(Runnable runnable) {
		try {
			SLOW_EXECUTOR.execute(runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.err.println("#execute#");
//		Thread.dumpStack();
	}
}
