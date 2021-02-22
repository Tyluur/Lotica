package com.runescape;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemsEquipIds;
import com.runescape.game.GameConstants;
import com.runescape.game.content.FishingSpotsHandler;
import com.runescape.game.content.FriendChatsManager;
import com.runescape.game.content.GlobalPlayers;
import com.runescape.game.content.RandomEventManager;
import com.runescape.game.content.bot.BotInitializer;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TreasureTrailTier;
import com.runescape.game.content.economy.treasure.TreasureTrailHandler;
import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.content.global.commands.CommandHandler;
import com.runescape.game.content.global.lottery.Lottery;
import com.runescape.game.content.global.wilderness.WildernessActivityManager;
import com.runescape.game.content.skills.mining.Mining;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.interaction.controllers.ControllerHandler;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.PowerupGenerator;
import com.runescape.game.interaction.cutscenes.CutscenesHandler;
import com.runescape.game.interaction.dialogues.DialogueHandler;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.combat.CombatScriptsHandler;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.item.ItemProperties;
import com.runescape.game.world.item.WorldFloorItems;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.network.LoticaChannelHandler;
import com.runescape.network.stream.incoming.IncomingStreamHandler;
import com.runescape.utility.ConfigurationParser;
import com.runescape.utility.ItemDefinitionLoader;
import com.runescape.utility.applications.console.listener.ConsoleListener;
import com.runescape.utility.cache.huffman.Huffman;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.logging.ErrLogger;
import com.runescape.utility.logging.OutLogger;
import com.runescape.utility.world.Censor;
import com.runescape.utility.world.map.MapArchiveKeys;
import com.runescape.utility.world.map.MapAreas;
import com.runescape.utility.world.map.MusicHints;
import com.runescape.utility.world.npc.NPCWalkingData;
import com.runescape.utility.world.object.ObjectRemoval;
import com.runescape.utility.world.object.ObjectSpawns;
import com.runescape.utility.world.player.DTRank;
import com.runescape.utility.world.player.PkRank;
import com.runescape.utility.world.player.StarterList;
import com.runescape.workers.boot.BootHandler;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.SystemOutLogger;
import com.runescape.workers.game.login.LoginRequestProcessor;

import java.io.IOException;

public final class Main {

	/**
	 * The instance of this class
	 */
	private static Main SINGLETON;

	/**
	 * The time at which this class was started
	 */
	private final long startTime;

	/**
	 * If the server was started
	 */
	private boolean started = false;

	private Main() {
		this.startTime = System.currentTimeMillis();
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("user.timezone", "EST");

		System.setOut(new OutLogger(System.out));
		System.setErr(new ErrLogger(System.err));

		System.out.println(GameConstants.SERVER_NAME + " is running on the " + (GameConstants.HOSTED ? "host" : "local") + " server.");

		Main main = get();
		main.initialize();
		main.performCompletionTasks();

		System.out.println("Game Server prepared in " + (System.currentTimeMillis() - main.getStartTime()) + " milliseconds.");
	}

	/**
	 * Gets the {@link #SINGLETON}
	 */
	public static Main get() {
		if (SINGLETON == null) {
			SINGLETON = new Main();
		}
		return SINGLETON;
	}

	/**
	 * This method will initialize all necessary data to the server from external locations.
	 */
	private void initialize() throws IOException {
		// cache is started before all else
		Cache.init();
		// work is then prepared
		BootHandler.addWork(() -> {
			try {
				CoresManager.init();
				World.init();
				ControllerHandler.init();
				WildernessActivityManager.getSingleton().load();
			} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}, () -> {
			PkRank.init();
			DTRank.init();
		}, () -> {
			MapArchiveKeys.init();
			MapAreas.init();
			Mining.load();
		}, () -> {
			ObjectRemoval.initialize();
			ObjectSpawns.init();
			FishingSpotsHandler.init();
			TreasureTrailTier.dumpRewardIds();
		}, () -> {
			TreasureTrailHandler.loadAll();
			AchievementHandler.loadAll(false);
			IncomingStreamHandler.loadAll();
		}, () -> {
			WorldFloorItems.setup();
			ItemProperties.loadProperties();
			CombatScriptsHandler.init();
		}, RegionBuilder::init, MusicHints::init, ItemsEquipIds::init, Huffman::init, () -> {
			GsonStartup.loadAll();
			CommandHandler.initialize();
		}, () -> {
			try {
				DialogueHandler.init();
				GlobalPlayers.loadOnlineToday();
				ConfigurationParser.loadConfiguration();// 10
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, () -> {
			Lottery.loadLottery();
			CutscenesHandler.init();
			FriendChatsManager.init();
			ClansManager.init();
			RandomEventManager.populateQuestionBank();
		}, () -> {
			Censor.init();
			PowerupGenerator.loadPowerups();
		}, () -> {
			StarterList.loadStarters();
			NPCWalkingData.loadList();
		}, () -> {
			QuestManager.initialize();
			InteractionEventManager.initialize();
		}, ItemDefinitionLoader::load);
		BootHandler.await();
	}

	/**
	 * Performs the tasks that take place after the server has been initialized
	 */
	public void performCompletionTasks() throws InterruptedException {
		BootHandler.finish();
//		MapMerging.start();
		// TODO this
		ConsoleListener.initializeConsoleListener();
		LoginRequestProcessor.getSingleton().init();
		LoticaChannelHandler.init();
		BotInitializer.initializeBots();
		started = true;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	public static void shutdown() {
		closeServices();
		System.exit(-1);
	}

	public static void closeServices() {
		CoresManager.shutdown();
		SystemOutLogger.flushAll();
	}

	/**
	 * Gets the started variable
	 */
	public boolean hasStarted() {
		return started;
	}
}
