package com.runescape.game.world;

import com.runescape.Main;
import com.runescape.game.GameConstants;
import com.runescape.game.content.global.minigames.GodWarsBosses;
import com.runescape.game.content.global.minigames.ZarosGodwars;
import com.runescape.game.content.global.minigames.clanwars.FfaZone;
import com.runescape.game.content.global.minigames.clanwars.RequestController;
import com.runescape.game.content.global.minigames.duel.DuelControler;
import com.runescape.game.content.skills.LivingRockCavern;
import com.runescape.game.content.skills.hunter.TrapAction.HunterNPC;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.EntityList;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.corp.CorporealBeast;
import com.runescape.game.world.entity.npc.dragons.KingBlackDragon;
import com.runescape.game.world.entity.npc.glacor.Glacor;
import com.runescape.game.world.entity.npc.godwars.Bosses;
import com.runescape.game.world.entity.npc.godwars.GodWarMinion;
import com.runescape.game.world.entity.npc.godwars.armadyl.ArmadylFaction;
import com.runescape.game.world.entity.npc.godwars.armadyl.KreeArra;
import com.runescape.game.world.entity.npc.godwars.bandos.BandosFaction;
import com.runescape.game.world.entity.npc.godwars.bandos.GeneralGraardor;
import com.runescape.game.world.entity.npc.godwars.saradomin.CommanderZilyana;
import com.runescape.game.world.entity.npc.godwars.saradomin.SaradominFaction;
import com.runescape.game.world.entity.npc.godwars.zammorak.KrilTstsaroth;
import com.runescape.game.world.entity.npc.godwars.zammorak.ZamorakFaction;
import com.runescape.game.world.entity.npc.godwars.zaros.Nex;
import com.runescape.game.world.entity.npc.godwars.zaros.NexMinion;
import com.runescape.game.world.entity.npc.kalph.KalphiteQueen;
import com.runescape.game.world.entity.npc.nomad.FlameVortex;
import com.runescape.game.world.entity.npc.nomad.Nomad;
import com.runescape.game.world.entity.npc.others.*;
import com.runescape.game.world.entity.npc.others.boss.DagganothKingNPC;
import com.runescape.game.world.entity.npc.slayer.AbyssalDemon;
import com.runescape.game.world.entity.npc.slayer.Strykewyrm;
import com.runescape.game.world.entity.npc.sorgar.Elemental;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.game.world.region.Region;
import com.runescape.game.world.route.Flags;
import com.runescape.utility.AntiFlood;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader.Direction;
import com.runescape.workers.db.ConnectionPool;
import com.runescape.workers.db.DatabaseConnection;
import com.runescape.workers.db.configuration.ConfigurationNode;
import com.runescape.workers.db.configuration.ConfigurationParser;
import com.runescape.workers.db.mysql.MySQLDatabaseConfiguration;
import com.runescape.workers.db.mysql.MySQLDatabaseConnection;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTasksManager;
import com.runescape.workers.tasks.impl.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This class handles all main world operations, lists the players and npcs in existence, and other necessary main World
 * functions.
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/30/2015
 */
public final class World {

	/** The {@code EntityList} of all players online */
	private static final EntityList<Player> players = new EntityList<>(GameConstants.PLAYERS_LIMIT, true);

	/** The {@code EntityList} of all npcs that exist. */
	private static final EntityList<NPC> npcs = new EntityList<>(GameConstants.NPCS_LIMIT, false);

	/** The map of regions, with the key being the regionId */
	private static final Map<Integer, Region> regions = Collections.synchronizedMap(new HashMap<>());

	public static int exiting_delay;

	public static long exiting_start;

	/**
	 * The MySQL Connection pool
	 */
	private static ConnectionPool<? extends DatabaseConnection> connectionPool;

	/**
	 * Initializing world processes, ticks, sql database information, and other background events
	 */
	public static void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		CoresManager.registerTasks();
		generateSystemTasks();
		loadConfiguration();
		LivingRockCavern.init();
	}

	/**
	 * Generates and registers all default system {@code WorldTask}s
	 */
	private static void generateSystemTasks() {
		try {
			WorldTasksManager.schedule(new InformationTabTick(), 3, 3);
			WorldTasksManager.schedule(new ShootingStarTick(), 5, 5);
			WorldTasksManager.schedule(new PunishmentTick(), 3, 3);
			WorldTasksManager.schedule(new TimedItemsTick(), 1, 1);
			WorldTasksManager.schedule(new TimedObjectHandling(), 5, 5);
			WorldTasksManager.schedule(new DailyEventTick(), 60, 60);
			WorldTasksManager.schedule(new ServerTipsTick(), 10, 10);
			WorldTasksManager.schedule(new LentItemsTick(), 3, 3);
			WorldTasksManager.schedule(new AntiVPNTick(), 10, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads server configuration.
	 *
	 * @throws IOException
	 * 		if an I/O error occurs.
	 * @throws ClassNotFoundException
	 * 		if a class loaded through reflection was not found.
	 * @throws IllegalAccessException
	 * 		if a class could not be accessed.
	 * @throws InstantiationException
	 * 		if a class could not be created.
	 */
	public static void loadConfiguration() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try (FileInputStream fis = new FileInputStream(GameConstants.SQL_FILE_PATH)) {
			ConfigurationParser parser = new ConfigurationParser(fis);
			ConfigurationNode mainNode = parser.parse();
			if (mainNode.has("database")) {
				ConfigurationNode databaseNode = mainNode.nodeFor("database");
				MySQLDatabaseConfiguration config = new MySQLDatabaseConfiguration();
				config.setHost(databaseNode.getString("host"));
				config.setPort(databaseNode.getInteger("port"));
				config.setDatabase(databaseNode.getString("database"));
				config.setUsername(databaseNode.getString("username"));
				config.setPassword(databaseNode.getString("password"));
				setConnectionPool(new ConnectionPool<MySQLDatabaseConnection>(config));
//				setConnectionPool(new ThreadedSQL(config, CoresManager.AVAILABLE_PROCESSORS).getConnectionPool());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Map<Integer, Region> getRegions() {
		return regions;
	}

	public static Region getRegion(int id) {
		try {
			return getRegion(id, false);
		} catch (Throwable e) {
			throw new IllegalStateException("Could not find region by id: " + id);
		}
	}

	public static Region getRegion(int id, boolean load) {
		Region region = regions.get(id);
		if (region == null) {
			region = new Region(id);
			regions.put(id, region);
		}
		if (load) {
			region.checkLoadMap();
		}
		return region;
	}

	public static void addPlayer(Player player) {
		try {
			players.add(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
		AntiFlood.add(player.getSession().getIP());
	}

	public static void removePlayer(Player player) {
		players.remove(player);
		AntiFlood.remove(player.getSession().getIP());
	}

	public static void addNPC(NPC npc) {
		try {
			npcs.add(npc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeNPC(NPC npc) {
		npcs.remove(npc);
	}

	public static NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		NPC n;
		if (id == 5079) {
			n = new HunterTrapNPC(HunterNPC.GREY_CHINCHOMPA, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5080) {
			n = new HunterTrapNPC(HunterNPC.RED_CHINCHOMPA, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5081) {
			n = new HunterTrapNPC(HunterNPC.FERRET, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6916) {
			n = new HunterTrapNPC(HunterNPC.GECKO, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 7272) {
			n = new HunterTrapNPC(HunterNPC.MONKEY, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 7272) {
			n = new HunterTrapNPC(HunterNPC.RACCOON, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5073) {
			n = new HunterTrapNPC(HunterNPC.CRIMSON_SWIFT, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5075) {
			n = new HunterTrapNPC(HunterNPC.GOLDEN_WARBLER, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5076) {
			n = new HunterTrapNPC(HunterNPC.COPPER_LONGTAIL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5074) {
			n = new HunterTrapNPC(HunterNPC.CERULEAN_TWITCH, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5072) {
			n = new HunterTrapNPC(HunterNPC.TROPICAL_WAGTAIL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 7031) {
			n = new HunterTrapNPC(HunterNPC.WIMPY_BIRD, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 5088) {
			n = new HunterTrapNPC(HunterNPC.BARB_TAILED_KEBBIT, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 5533 && id <= 5558) {
			n = new Elemental(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 7134) {
			n = new Bork(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 1615) {
			n = new AbyssalDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		} else if (id == 9441) {
			n = new FlameVortex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 8832 && id <= 8834) {
			n = new LivingRock(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 13465 && id <= 13481) {
			n = new Revenant(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 14301) {
			n = new Glacor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		} else if (id == 6078 || id == 6079 || id == 4292 || id == 4291 || id == 6080 || id == 6081) {
			n = new Cyclopse(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		} else if (id == 1158 || id == 1160) {
			n = new KalphiteQueen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 8528 && id <= 8532) {
			n = new Nomad(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 6210 && id <= 6221) {
			n = new ZamorakFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 3406 || id >= 6254 && id <= 6259) {
			n = new SaradominFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 6228 && id <= 6246) {
			n = new ArmadylFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 6268 && id <= 6283 || id == 9184) {
			n = new BandosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6261 || id == 6263 || id == 6265) {
			n = GodWarsBosses.graardorMinions[(id - 6261) / 2] = new GodWarMinion(Bosses.BANDOS, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6260) {
			n = new GeneralGraardor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6222) {
			n = new KreeArra(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6223 || id == 6225 || id == 6227) {
			n = GodWarsBosses.armadylMinions[(id - 6223) / 2] = new GodWarMinion(Bosses.ARMADYL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6203) {
			n = new KrilTstsaroth(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6204 || id == 6206 || id == 6208) {
			n = GodWarsBosses.zamorakMinions[(id - 6204) / 2] = new GodWarMinion(Bosses.ZAMORAK, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 50 || id == 2642) {
			n = new KingBlackDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 9462 && id <= 9467) {
			n = new Strykewyrm(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
			n.setSpawned(false);
		} else if (id == 6248 || id == 6250 || id == 6252) {
			n = GodWarsBosses.commanderMinions[(id - 6248) / 2] = new GodWarMinion(Bosses.SARADOMIN, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6247) {
			n = new CommanderZilyana(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 14688 && id <= 14701) {
			n = new PolyporeCreature(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 8133) {
			n = new CorporealBeast(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 13447) {
			n = ZarosGodwars.nex = new Nex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 13451) {
			n = ZarosGodwars.fumus = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 13452) {
			n = ZarosGodwars.umbra = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 13453) {
			n = ZarosGodwars.cruor = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 13454) {
			n = ZarosGodwars.glacies = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 14256) {
			n = new Lucien(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 8335) {
			n = new MercenaryMage(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 8349 || id == 8450 || id == 8451) {
			n = new TormentedDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 15149) {
			n = new MasterOfFear(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 1266 || id == 1268 || id == 2453 || id == 2886) {
			n = new RockCrabs(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 1926 || id == 1931) {
			n = new BanditCampBandits(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 3847) {
			n = new SeaTrollQueen(id, tile);
		} else if (id >= 912 && id <= 914) {
			n = new MageArenaNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id >= 2881 && id <= 2883) {
			n = new DagganothKingNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else {
			n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);

			// bats at mb
			if ((id == 183 || id == 78) && tile.getRegionId() == 12349) {
				n.setForceAgressive(true);
			} else if (n.getName().equalsIgnoreCase("spinolyp")) {
				n.setForceAgressive(true);
				n.setForceTargetDistance(16);
			}
		}
		return n;
	}

	public static NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, Direction direction) {
		NPC npc = spawnNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
		npc.setFaceDirection(direction);
		return npc;
	}

	/*
	 * check if the entity region changed because moved or teled then we update
	 * it
	 */
	public static final void updateEntityRegion(Entity entity) {
		if (entity.hasFinished()) {
			if (entity instanceof Player) {
				getRegion(entity.getLastRegionId()).removePlayerIndex(entity.player());
			} else {
				getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
			}
			return;
		}
		int regionId = entity.getRegionId();
		if (entity.getLastRegionId() != regionId) { // map region entity at
			// changed
			if (entity instanceof Player) {
				if (entity.getLastRegionId() > 0) {
					getRegion(entity.getLastRegionId()).removePlayerIndex(entity.player());
				}
				Region region = getRegion(regionId);
				region.addPlayerIndex(entity.player());
				Player player = (Player) entity;
				int musicId = region.getMusicId();
				if (musicId != -1) {
					player.getMusicsManager().checkMusic(musicId);
				}
				player.getControllerManager().moved();
				if (player.hasStarted()) {
					checkControlersAtMove(player);
				}
			} else {
				if (entity.getLastRegionId() > 0) {
					getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
				}
				getRegion(regionId).addNPCIndex(entity.getIndex());
			}
			entity.checkMultiArea();
			entity.setLastRegionId(regionId);
		} else {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				player.getControllerManager().moved();
				if (player.hasStarted()) {
					checkControlersAtMove(player);
				}
			}
			entity.checkMultiArea();
		}
	}

	public static void checkControlersAtMove(Player player) {
		if (player.getControllerManager().getController() == null) {
			if (RequestController.inWarRequest(player)) {
				player.getControllerManager().startController("clan_wars_request");
			} else if (DuelControler.isAtDuelArena(player)) {
				player.getControllerManager().startController("DuelControler");
			} else if (Wilderness.isAtWild(player)) {
				player.getControllerManager().startController("Wilderness");
			} else if (FfaZone.inArea(player)) {
				player.getControllerManager().startController("clan_wars_ffa");
			}
		}
	}

	/*
	 * checks clip
	 */
	public static boolean canMoveNPC(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++) {
			for (int tileY = y; tileY < y + size; tileY++) {
				if (getMask(plane, tileX, tileY) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * checks clip
	 */
	public static boolean isNotCliped(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++) {
			for (int tileY = y; tileY < y + size; tileY++) {
				if ((getMask(plane, tileX, tileY) & 2097152) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isTileFree(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++) {
			for (int tileY = y; tileY < y + size; tileY++) {
				if (!isFloorFree(plane, tileX, tileY) || !isWallsFree(plane, tileX, tileY)) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isFloorFree(int plane, int x, int y) {
		return (getMask(plane, x, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ)) == 0;
	}

	public static boolean isWallsFree(int plane, int x, int y) {
		return (getMask(plane, x, y) & (Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST | Flags.WALLOBJ_EAST | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST)) == 0;
	}

	public static int getMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null) {
			return -1;
		}
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMask(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static void setMask(int plane, int x, int y, int mask) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null) {
			return;
		}
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
	}

	public static int getRotation(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null) {
			return 0;
		}
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipedOnlyMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null) {
			return -1;
		}
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMaskClipedOnly(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final boolean checkProjectileStep(int plane, int x, int y, int dir, int size) {
		int xOffset = Utils.DIRECTION_DELTA_X[dir];
		int yOffset = Utils.DIRECTION_DELTA_Y[dir];
		/*
		 * int rotation = getRotation(plane,x+xOffset,y+yOffset); if(rotation !=
		 * 0) { dir += rotation; if(dir >= Utils.DIRECTION_DELTA_X.length) dir =
		 * dir - (Utils.DIRECTION_DELTA_X.length-1); xOffset =
		 * Utils.DIRECTION_DELTA_X[dir]; yOffset = Utils.DIRECTION_DELTA_Y[dir];
		 * }
		 */
		if (size == 1) {
			int mask = getClipedOnlyMask(plane, x + Utils.DIRECTION_DELTA_X[dir], y + Utils.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0) {
				return (mask & 0x42240000) == 0;
			}
			if (xOffset == 1 && yOffset == 0) {
				return (mask & 0x60240000) == 0;
			}
			if (xOffset == 0 && yOffset == -1) {
				return (mask & 0x40a40000) == 0;
			}
			if (xOffset == 0 && yOffset == 1) {
				return (mask & 0x48240000) == 0;
			}
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0) {
				return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			}
			if (xOffset == 1 && yOffset == 0) {
				return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			}
			if (xOffset == 0 && yOffset == -1) {
				return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			}
			if (xOffset == 0 && yOffset == 1) {
				return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			}
			if (xOffset == -1 && yOffset == -1) {
				return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
			}
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0 || (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0) {
						return false;
					}
				}
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0) {
						return false;
					}
				}
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0 || (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0) {
						return false;
					}
				}
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0) {
						return false;
					}
				}
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static final boolean checkWalkStep(int plane, int x, int y, int dir, int size) {
		return checkWalkStep(plane, x, y, Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], size);
	}

	public static final boolean checkWalkStep(int plane, int x, int y, int xOffset, int yOffset, int size) {
		if (size == 1) {
			int mask = getMask(plane, x + xOffset, y + yOffset);
			if (xOffset == -1 && yOffset == 0) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0;
			}
			if (xOffset == 1 && yOffset == 0) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0;
			}
			if (xOffset == 0 && yOffset == -1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
			}
			if (xOffset == 0 && yOffset == 1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
			}
			if (xOffset == -1 && yOffset == -1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0) {
				return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0;
			}
			if (xOffset == 1 && yOffset == 0) {
				return (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
			}
			if (xOffset == 0 && yOffset == -1) {
				return (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0;
			}
			if (xOffset == 0 && yOffset == 1) {
				return (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
			}
			if (xOffset == -1 && yOffset == -1) {
				return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 2, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 2, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
			}
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x - 1, -1 + (y + size)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getMask(plane, x + size, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0 || (getMask(plane, x + size, y - (-size + 1)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x + size - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getMask(plane, x, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, x + (size - 1), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
					if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getMask(plane, x - 1, y + (-1 + sizeOffset)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, sizeOffset - 1 + x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getMask(plane, x + size, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getMask(plane, x + size, sizeOffset + (-1 + y)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getMask(plane, x - 1, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, -1 + (x + sizeOffset), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
						return false;
					}
				}
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getMask(plane, x + size, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
					return false;
				}
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
					if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static final boolean containsPlayer(String username) {
		for (Player p2 : players) {
			if (p2 == null) {
				continue;
			}
			if (p2.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public static Player getPlayer(String username) {
		for (Player player : getPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.getUsername().equalsIgnoreCase(username)) {
				return player;
			}
		}
		return null;
	}

	public static final Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utils.formatPlayerNameForDisplay(username);
		for (Player player : getPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.getUsername().equalsIgnoreCase(formatedUsername) || player.getDisplayName().equalsIgnoreCase(formatedUsername)) {
				return player;
			}
		}
		return null;
	}

	/**
	 * Converts the {@link #players} {@code EntityList} to a {@code Stream} of players
	 *
	 * @return A {@code Stream}
	 */
	public static Stream<Player> players() {
		return players.stream().filter(p -> p != null && p.hasStarted() && !p.hasFinished());
	}

	public static EntityList<Player> getPlayers() {
		return players;
	}

	public static int getPlayerCount() {
		return players.size();
	}

	public static EntityList<NPC> getNPCs() {
		return npcs;
	}

	private World() {

	}

	public static final void safeShutdown(int delay) {
		exiting_start = Utils.currentTimeMillis();
		exiting_delay = delay;
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished()) {
				continue;
			}
			player.getPackets().sendSystemUpdate(delay);
		}
		CoresManager.schedule(() -> {
			try {
				CoresManager.saveFiles(true);
				Main.shutdown();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, delay, TimeUnit.SECONDS);
	}

	public static final void sendObjectAnimation(WorldObject object, Animation animation) {
		sendObjectAnimation(null, object, animation);
	}

	public static final void sendObjectAnimation(Entity creator, WorldObject object, Animation animation) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object)) {
					continue;
				}
				player.getPackets().sendObjectAnimation(object, animation);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null) {
					continue;
				}
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object)) {
						continue;
					}
					player.getPackets().sendObjectAnimation(object, animation);
				}
			}
		}
	}

	public static final void sendGraphics(Entity creator, Graphics graphics, WorldTile tile) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile)) {
					continue;
				}
				player.getPackets().sendGraphics(graphics, tile);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null) {
					continue;
				}
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile)) {
						continue;
					}
					player.getPackets().sendGraphics(graphics, tile);
				}
			}
		}
	}

	public static final void sendProjectile(Entity shooter, WorldTile startTile, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null) {
				continue;
			}
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
					continue;
				}
				player.getPackets().sendProjectile(null, startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(WorldTile shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : receiver.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null) {
				continue;
			}
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
					continue;
				}
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(Entity shooter, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null) {
				continue;
			}
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
					continue;
				}
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
			}
		}
	}

	public static final void sendProjectile(Entity shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		if (shooter == null || shooter.getMapRegionsIds() == null) {
			return;
		}
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null) {
				continue;
			}
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
					continue;
				}
				int size = shooter.getSize();
				player.getPackets().sendProjectile(receiver, new WorldTile(shooter.getCoordFaceX(size), shooter.getCoordFaceY(size), shooter.getPlane()), receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, size);
			}
		}
	}

	public static final boolean isMultiArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3462 && destX <= 3511 && destY >= 9481 && destY <= 9521 && tile.getPlane() == 0) // kalphite
				       // queen
				       // lair
				       || (destX >= 4540 && destX <= 4799 && destY >= 5052 && destY <= 5183 && tile.getPlane() == 0) // thzaar
				       // city
					   || tile.getRegionId() == 11051
				       || tile.getRegionId() == 16729 // glacors
				       || tile.getRegionId() == 11589 // dags
				       || tile.getRegionId() == 10894 // monkey skeles
				       || tile.getRegionId() == 11573 // sea troll queen
				       || tile.getRegionId() == 10554 || tile.getRegionId() == 10810 // rock crabs
				       || (destX >= 1721 && destX <= 1791 && destY >= 5123 && destY <= 5249) // mole
				       || (destX >= 3029 && destX <= 3374 && destY >= 3759 && destY <= 3903)// wild
				       || (destX >= 2250 && destX <= 2280 && destY >= 4670 && destY <= 4720) || (destX >= 3198 && destX <= 3380 && destY >= 3904 && destY <= 3970) || (destX >= 3191 && destX <= 3326 && destY >= 3510 && destY <= 3759) || (destX >= 2987 && destX <= 3006 && destY >= 3912 && destY <= 3937) || (destX >= 2245 && destX <= 2295 && destY >= 4675 && destY <= 4720) || (destX >= 2450 && destX <= 3520 && destY >= 9450 && destY <= 9550) || (destX >= 3006 && destX <= 3071 && destY >= 3602 && destY <= 3710) || (destX >= 3134 && destX <= 3192 && destY >= 3519 && destY <= 3646) || (destX >= 2815 && destX <= 2966 && destY >= 5240 && destY <= 5375)// wild
				       || (destX >= 2840 && destX <= 2950 && destY >= 5190 && destY <= 5230) // godwars
				       || (destX >= 3547 && destX <= 3555 && destY >= 9690 && destY <= 9699) // zaros
				       || (destX >= 1490 && destX <= 1515 && destY >= 4696 && destY <= 4714) // chaos dwarf battlefield
				       // godwars
				       || KingBlackDragon.atKBD(tile) // King Black Dragon lair
				       || TormentedDemon.atTD(tile) // Tormented demon's area
				       || Bork.atBork(tile) // Bork's area
				       || tile.getRegionId() == 12590 || (destX >= 2970 && destX <= 3000 && destY >= 4365 && destY <= 4400)// corp
				       || (destX >= 3195 && destX <= 3327 && destY >= 3520 && destY <= 3970 || (destX >= 2376 && 5127 >= destY && destX <= 2422 && 5168 <= destY)) || (destX >= 2374 && destY >= 5129 && destX <= 2424 && destY <= 5168) // pits
				       || (destX >= 2622 && destY >= 5696 && destX <= 2573 && destY <= 5752) // torms
				       || (destX >= 2368 && destY >= 3072 && destX <= 2431 && destY <= 3135) // castlewars
				       // out
				       || (destX >= 2365 && destY >= 9470 && destX <= 2436 && destY <= 9532) // castlewars
				       || (destX >= 2948 && destY >= 5537 && destX <= 3071 && destY <= 5631) // Risk
				       // ffa.
				       || (destX >= 2756 && destY >= 5537 && destX <= 2879 && destY <= 5631) // Safe
				       // ffa
				       || tile.getRegionId() == 1089

				       || tile.getRegionId() == 12341 || (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175 && (tile.getY() >= 10066 || tile.getX() >= 3094)) // fortihrny
				// dungeon
				;
		// in

		// multi
	}

	public static final boolean isPvpArea(WorldTile tile) {
		return Wilderness.isAtWild(tile);
	}

	public static void sendWorldMessage(String message, boolean forStaff) {
		for (Player p : World.getPlayers()) {
			if (p == null || !p.isRunning() || (forStaff && !p.isStaff())) {
				continue;
			}
			p.getPackets().sendGameMessage(message);
		}
	}

	public static final void sendProjectile(WorldObject object, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startOffset) {
		for (Player pl : players) {
			if (pl == null || !pl.withinDistance(object, 20)) {
				continue;
			}
			pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
		}
	}

	/**
	 * @return the connectionPool
	 */
	public static ConnectionPool<? extends DatabaseConnection> getConnectionPool() {
		return connectionPool;
	}

	/**
	 * @param connectionPool
	 * 		the connectionPool to set
	 */
	public static void setConnectionPool(ConnectionPool<? extends DatabaseConnection> connectionPool) {
		World.connectionPool = connectionPool;
	}

	public static final boolean isSpawnedObject(WorldObject object) {
		return getRegion(object.getRegionId()).getSpawnedObjects().contains(object);
	}

	public static final void spawnObject(WorldObject object) {
		getRegion(object.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), false);
	}

	public static final void unclipTile(WorldTile tile) {
		getRegion(tile.getRegionId()).unclip(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
	}

	public static final void removeObject(WorldObject object) {
		getRegion(object.getRegionId()).removeObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion());
	}

	public static final void spawnObjectTemporary(final WorldObject object, long time) {
		spawnObject(object);
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					if (!World.isSpawnedObject(object)) {
						return;
					}
					removeObject(object);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

		}, time, TimeUnit.MILLISECONDS);
	}

	public static final boolean removeObjectTemporary(final WorldObject object, long time) {
		removeObject(object);
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					spawnObject(object);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

		}, time, TimeUnit.MILLISECONDS);
		return true;
	}

	public static final void spawnTempGroundObject(final WorldObject object, final int replaceId, long time) {
		spawnObject(object);
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					removeObject(object);
					addGroundItem(new Item(replaceId), object, null, false, 180);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, time, TimeUnit.MILLISECONDS);
	}

	public static final WorldObject getStandartObject(WorldTile tile) {
		return getRegion(tile.getRegionId()).getStandardObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
	}

	public static final WorldObject getObjectWithType(WorldTile tile, int type) {
		return getRegion(tile.getRegionId()).getObjectWithType(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), type);
	}

	public static final WorldObject getObjectWithSlot(WorldTile tile, int slot) {
		return getRegion(tile.getRegionId()).getObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot);
	}

	public static final boolean containsObjectWithId(WorldTile tile, int id) {
		return getRegion(tile.getRegionId()).containsObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
	}

	public static final WorldObject getObjectWithId(WorldTile tile, int id) {
		return getRegion(tile.getRegionId()).getObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
	}

	public static void addGroundItem(final Item item, final WorldTile tile) {
		// adds item, not invisible, no owner, no time to disapear
		addGroundItem(item, tile, null, false, -1, 2, -1);
	}

	public static void addGroundItem(final Item item, final WorldTile tile, final Player owner, boolean invisible, long hiddenTime) {
		addGroundItem(item, tile, owner, invisible, hiddenTime, 2, 150);
	}

	public static FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner, boolean invisible, long hiddenTime, int type) {
		return addGroundItem(item, tile, owner, invisible, hiddenTime, type, 150);
	}

	public static void turnPublic(FloorItem floorItem, int publicTime) {
		if (!floorItem.isInvisible()) {
			return;
		}
		int regionId = floorItem.getTile().getRegionId();
		final Region region = getRegion(regionId);
		if (!region.forceGetFloorItems().contains(floorItem)) {
			return;
		}
		Player realOwner = floorItem.hasOwner() ? World.getPlayer(floorItem.getOwner().getUsername()) : null;
		if (!ItemConstants.isTradeable(floorItem)) {
			region.forceGetFloorItems().remove(floorItem);
			if (realOwner != null) {
				if (realOwner.getMapRegionsIds().contains(regionId) && realOwner.getPlane() == floorItem.getTile().getPlane()) {
					realOwner.getPackets().sendRemoveGroundItem(floorItem);
				}
			}
			return;
		}
		floorItem.setInvisible(false);
		for (Player player : players) {
			if (player == null || player == realOwner || !player.hasStarted() || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId)) {
				continue;
			}
			player.getPackets().sendGroundItem(floorItem);
		}
		// disapears after this time
		if (publicTime != -1) {
			removeGroundItem(floorItem, publicTime);
		}
	}

	public static final void addGroundItemForever(Item item, final WorldTile tile) {
		int regionId = tile.getRegionId();
		final FloorItem floorItem = new FloorItem(item, tile, true);
		final Region region = getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		for (Player player : players) {
			if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId)) {
				continue;
			}
			player.getPackets().sendGroundItem(floorItem);
		}
	}

	public static void addGroundItem(Item item, WorldTile tile, Player owner, boolean invisible, int hiddenTime) {
		addGroundItem(item, tile, owner, invisible, hiddenTime, 2, 150);
	}

	/*
	 * type 0 - gold if not tradeable type 1 - gold if destroyable type 2 - no
	 * gold
	 */
	public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner, boolean invisible, long hiddenTime, int type, final int publicTime) {
		try {
			if (type != 2) {
				if ((type == 0 && !ItemConstants.isTradeable(item))) {
					int price = item.getDefinitions().getValue();
					if (price <= 0) {
						return null;
					}
					item.setId(995);
					item.setAmount(price);
				}
			}
			// type will be 3 when players dont have room for the item, so barrows wont degrade in that case
			if (type != 3) {
				int degradeId = ItemConstants.getBarrowsDegradedId(item);
				if (degradeId != -1) {
					item.setId(degradeId);
				}
			}
			final FloorItem floorItem = new FloorItem(item, tile, owner, owner != null, invisible);
			final Region region = getRegion(tile.getRegionId());
			region.forceGetFloorItems().add(floorItem);
			if (invisible) {
				if (owner != null) {
					owner.getPackets().sendGroundItem(floorItem);
				}
				if (hiddenTime != -1) {
					CoresManager.schedule(() -> {
						try {
							turnPublic(floorItem, publicTime);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}, hiddenTime, TimeUnit.SECONDS);
				}
			} else {
				// visible
				int regionId = tile.getRegionId();
				for (Player player : players) {
					if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId)) {
						continue;
					}
					player.getPackets().sendGroundItem(floorItem);
				}
				// disapears after this time
				if (publicTime != -1) {
					removeGroundItem(floorItem, publicTime);
				}
			}
			return floorItem;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final void updateGroundItem(Item item, final WorldTile tile, final Player owner) {
		final FloorItem floorItem = World.getRegion(tile.getRegionId()).getFloorItem(item.getId(), tile, owner);
		if (floorItem == null) {
			addGroundItem(item, tile, owner, true, 360);
			return;
		}
		floorItem.setAmount(floorItem.getAmount() + item.getAmount());
		owner.getPackets().sendRemoveGroundItem(floorItem);
		owner.getPackets().sendGroundItem(floorItem);
	}

	public static final boolean removeTemporaryObject(final WorldObject object, long time) {
		removeObject(object);
		CoresManager.schedule(() -> {
			try {
				spawnObject(object);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, time, TimeUnit.MILLISECONDS);
		return true;
	}

	public static final void spawnTemporaryObject(final WorldObject object, long time) {
		spawnObject(object);
		CoresManager.schedule(() -> {
			try {
				if (!World.isSpawnedObject(object)) {
					return;
				}
				removeObject(object);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, time, TimeUnit.MILLISECONDS);
	}

	private static final void removeGroundItem(final FloorItem floorItem, long publicTime) {
		CoresManager.schedule(() -> {
			try {
				int regionId = floorItem.getTile().getRegionId();
				Region region = getRegion(regionId);
				if (!region.forceGetFloorItems().contains(floorItem)) {
					return;
				}
				region.forceGetFloorItems().remove(floorItem);
				for (Player player : World.getPlayers()) {
					if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId)) {
						continue;
					}
					player.getPackets().sendRemoveGroundItem(floorItem);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, publicTime, TimeUnit.SECONDS);
	}

	public static final boolean removeGroundItem(Player player, FloorItem floorItem) {
		return removeGroundItem(player, floorItem, true);
	}

	public static final boolean removeGroundItem(Player player, final FloorItem floorItem, boolean add) {
		int regionId = floorItem.getTile().getRegionId();
		Region region = getRegion(regionId);
		if (!region.forceGetFloorItems().contains(floorItem)) {
			return false;
		}
		if (player.getInventory().getFreeSlots() == 0 && (!floorItem.getDefinitions().isStackable() || !player.getInventory().containsItem(floorItem.getId(), 1))) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		region.forceGetFloorItems().remove(floorItem);
		if (add) {
			player.getInventory().addItemDrop(floorItem);
		}
		if (floorItem.isInvisible()) {
			player.getPackets().sendRemoveGroundItem(floorItem);
			return true;
		} else {
			for (Player p2 : World.getPlayers()) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished() || p2.getPlane() != floorItem.getTile().getPlane() || !p2.getMapRegionsIds().contains(regionId)) {
					continue;
				}
				p2.getPackets().sendRemoveGroundItem(floorItem);
			}
			if (floorItem.isForever()) {
				CoresManager.schedule((Runnable) () -> {
					try {
						addGroundItemForever(floorItem, floorItem.getTile());
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}, 60, TimeUnit.SECONDS);
			}
			return true;
		}
	}

	public static final WorldObject getStandardObject(WorldTile tile) {
		return getRegion(tile.getRegionId()).getStandardObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
	}

	public static final boolean containsObjectWithId(int id, WorldTile tile) {
		return getRegion(tile.getRegionId()).containsObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
	}

}
