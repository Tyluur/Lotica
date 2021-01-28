package com.runescape.game.interaction.controllers.impl.nmz.powerup;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.impl.nmz.NMZController;
import com.runescape.game.interaction.controllers.impl.nmz.NMZInstance;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.impl.ZapperPowerup;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.region.Region;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/23/2016
 */
public class PowerupGenerator {

	/**
	 * The time between possible powerup spawns
	 */
	private static final long TIME_BETWEEN_POWERUP_SPAWNS = TimeUnit.SECONDS.toMillis(180);

	/**
	 * The list of possible powerups
	 */
	private static final List<NMZPowerup> POWERUP_LIST = new ArrayList<>();

	/**
	 * The possible tiles that powerups can be spawned at
	 */
	private static final int[][] SPAWN_TILES = new int[][] { { 25, 30 }, { 10, 30 }, { 10, 15 }, { 37, 20 }, { 37, 31 }, { 25, 15 }, { 10, 9 }, };

	/**
	 * The list of all powerups that are active in the game.
	 */
	private final List<NMZPowerup> activePowerups;

	/**
	 * The instance of the game
	 */
	private final NMZInstance game;

	/**
	 * The last time powerups were introduced to the game
	 */
	private long lastTimePowerupsIntroduced;

	public PowerupGenerator(NMZInstance game) {
		this.activePowerups = new ArrayList<>();
		this.game = game;
	}

	/**
	 * Loads all powerups in the subdirectory 'impl' to the {@link #POWERUP_LIST} list
	 */
	public static void loadPowerups() {
		try {
			POWERUP_LIST.addAll(Utils.getClassesInDirectory(PowerupGenerator.class.getPackage().getName() + ".impl").stream().map(clazz -> (NMZPowerup) clazz).collect(Collectors.toList()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a list of random powerups
	 *
	 * @param amount
	 * 		The size of the list
	 */
	private static List<NMZPowerup> getRandomPowerups(int amount) {
		List<NMZPowerup> powerups = new ArrayList<>();
		List<NMZPowerup> localPowerupsList = new ArrayList<>(POWERUP_LIST);

		for (int i = 0; i < amount; i++) {
			Collections.shuffle(localPowerupsList);
			NMZPowerup powerup = localPowerupsList.get(0);
			int count = 0;
			while (powerups.contains(powerup)) {
				if (count++ == 10) {
					break;
				}
				Collections.shuffle(localPowerupsList);
				powerup = localPowerupsList.get(0);
			}
			powerups.add(powerup);
		}
		return powerups;
	}

	/**
	 * If we should generate powerups
	 */
	public boolean shouldGeneratePowerups() {
		//  not spawning more if there are any ingame
		if (!activePowerups.isEmpty()) {
			return false;
		}
		long timePassed = System.currentTimeMillis() - game.getStartTime();

		//  the game must have been happening for a while
		if (GameConstants.DEBUG || timePassed >= TIME_BETWEEN_POWERUP_SPAWNS) {
			boolean minutesPassed = GameConstants.DEBUG || (System.currentTimeMillis() - lastTimePowerupsIntroduced) >= TIME_BETWEEN_POWERUP_SPAWNS;
			//  if we havent spawned powerups ever, or if the last time we spawned powerups was a while ago
			if (lastTimePowerupsIntroduced == -1 || minutesPassed) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Generates the powerups by finding a random amount of powerups to add, creating a list of random powerups by that
	 * size, and then initiating them into the game
	 */
	public void generatePowerups() {
		int amountToSpawn = Utils.random(2, 4);
		List<NMZPowerup> listOfPowerups = getRandomPowerups(amountToSpawn);
		listOfPowerups.forEach(this::initiatePowerup);
		WorldObject object = getObjectByPowerup(listOfPowerups.get(listOfPowerups.size() - 1));
		if (object != null) { game.addMinimapMarker(object.getWorldTile()); }
		this.lastTimePowerupsIntroduced = System.currentTimeMillis();
	}

	/**
	 * Initiates a powerup by spawning it into the game on a random tile selected from {@link #SPAWN_TILES}
	 *
	 * @param powerup
	 * 		The powerup
	 */
	private void initiatePowerup(NMZPowerup powerup) {
		int[] coords = Utils.randomArraySlot(SPAWN_TILES);
		WorldTile tile = game.getWorldTile(coords[0], coords[1]);
		while (getPowerupAtTile(tile) != null) {
			coords = Utils.randomArraySlot(SPAWN_TILES);
			tile = game.getWorldTile(coords[0], coords[1]);

			if (GameConstants.DEBUG) {
				System.out.println("We already had a powerup at tile\t" + tile + " - not spawning it");
			}
		}
		game.getTeam().forEach(player -> player.sendMessage("<col=" + ChatColors.BLUE + ">A powerup has spawned</col>: <col=" + ChatColors.MAROON + ">" + powerup.name() + "."));
		World.spawnObject(new WorldObject(powerup.getObjectId(), 10, 0, tile));
		activePowerups.add(powerup);
	}

	/**
	 * Gets the active powerups list
	 */
	public List<NMZPowerup> getActivePowerups() {
		return activePowerups;
	}

	/**
	 * Gets the powerup at a certain tile
	 *
	 * @param tile
	 * 		The tile
	 */
	public Object[] getPowerupAtTile(WorldTile tile) {
		Region region = World.getRegion(game.getMiddleTile().getRegionId());
		if (region == null) {
			if (GameConstants.DEBUG) { System.out.println("no region instance..."); }
			return null;
		}

		List<WorldObject> objects = region.getSpawnedObjects();
		for (WorldObject object : objects) {
			if (object.getWorldTile().matches(tile)) {
				for (NMZPowerup powerup : activePowerups) {
					if (powerup.getObjectId() == object.getId()) {
						return new Object[] { powerup, object };
					}
				}
			}
		}
		return null;
	}

	/**
	 * Applys the effect of a powerup
	 *
	 * @param player
	 * 		The player who got the powerup
	 * @param powerup
	 * 		The powerup
	 * @param object
	 * 		The object of the powerup, used to figure out the tile
	 */
	public void applyPowerupEffect(Player player, NMZPowerup powerup, WorldObject object) {
		if (!activePowerups.contains(powerup)) {
			return;
		}
		player.sendMessage(powerup.activationMessage());
		World.removeObject(object);
		powerup.onPickup(player);
		activePowerups.remove(powerup);
		game.removeMinimapMarker(object.getWorldTile());

		refreshHintIcons();

		// the zapper powerup doesnt get stored
		if (!powerup.equals(powerupByClass(ZapperPowerup.class))) {
			player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> nmz.getPowerups().add(powerup));
		}
	}

	/**
	 * Refreshes the hint icons
	 */
	public void refreshHintIcons() {
		if (activePowerups.size() > 0) {
			NMZPowerup powerup = activePowerups.get(0);
			WorldObject object = getObjectByPowerup(powerup);
			if (object == null) {
				System.out.println("Couldn't find object from powerup\t" + powerup);
				return;
			}
			game.addMinimapMarker(object.getWorldTile());
		} else {
			game.removeAllMarkers();
		}
	}

	/**
	 * Gets a world object {@code Object} based on a {@code NMZPowerup} instance
	 *
	 * @param powerup
	 * 		The powerup instance
	 */
	private WorldObject getObjectByPowerup(NMZPowerup powerup) {
		Region region = World.getRegion(game.getMiddleTile().getRegionId());
		if (region == null) {
			return null;
		}
		for (WorldObject object : region.getSpawnedObjects()) {
			if (object.getId() == powerup.getObjectId()) {
				return object;
			}
		}
		return null;
	}

	/**
	 * Gets a {@code NMZPowerup} {@code Object} of a powerup
	 *
	 * @param clazz
	 * 		The class
	 */
	public static NMZPowerup powerupByClass(Class<?> clazz) {
		for (NMZPowerup powerup : POWERUP_LIST) {
			if (powerup.getClass().equals(clazz)) {
				return powerup;
			}
		}
		return null;
	}

}
