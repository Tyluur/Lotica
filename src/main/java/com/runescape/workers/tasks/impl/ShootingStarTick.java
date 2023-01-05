package com.runescape.workers.tasks.impl;

import com.runescape.game.content.skills.mining.CrashedStarMining;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class ShootingStarTick extends WorldTask {

	/**
	 * The possible ids of the star
	 */
	public static final int[] STAR_IDS = { 38660, 38661, 38662, 38663, 38664, 38665, 38666, 38667, 38668 };

	/**
	 * The amount of coins we get for tagging the star
	 */
	public static final int COIN_SUM = 100_000;

	/**
	 * Junk rewards
	 */
	public static final int[] DECENT_REWARDS = new int[] { 10476, 1618, 868, 15273 };

	/**
	 * Common rewards
	 */
	public static final int[] COMMON_REWARDS = new int[] { 1632, 2890, 18699, 20458, 9729, 2579, 7398, 7399, 7400, 14497, 14499, 14501 };

	/**
	 * Rare rewards
	 */
	public static final int[] RARE_REWARDS = new int[] { 15259, 4153, 21477, 21478, 21479, 2581, 2577 };

	/**
	 * The minutes the star exists for
	 */
	private static final int MINUTES_EXISTANT = 30;

	/**
	 * The last time the star was spawned at
	 */
	private long lastSpawnedAt;

	/**
	 * The crashed star
	 */
	private ShootingStar star;

	/**
	 * The location the star was spawned at
	 */
	private String spawnLocation;

	@Override
	public void run() {
		// we must have 1 player online for a crashed star to be possible
		if (World.getPlayers().size() > 0) {
			// if there is no current star
			if (star == null) {
				// if we must spawn a new star
				if (lastSpawnedAt == -1 || (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastSpawnedAt) >= 2)) {
					lastSpawnedAt = System.currentTimeMillis();
					constructStar();
				}
			} else {
				if (star.shouldRemove()) {
					destroyStar();
					return;
				}
				if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - star.getSpawnedAt()) >= MINUTES_EXISTANT) {
					destroyStar();
				}
			}
		}
	}

	/**
	 * Makes the crashed star
	 */
	private void constructStar() {
		try {
			StarLocations starLocation = StarLocations.values()[Utils.random(StarLocations.values().length)];
			WorldTile[] possibleTiles = starLocation.getTiles();
			int index = Utils.random(possibleTiles.length);
			WorldTile spawnTile = possibleTiles[index];
			spawnLocation = starLocation.getLocationNames()[index];

			World.spawnObject(star = new ShootingStar(STAR_IDS[0], 10, 1, spawnTile));
//			World.sendWorldMessage("<img=5><col=E65D02>A shooting star has just landed near " + spawnLocation + "!", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroys the star and removes it from the world. Everyone online also receives a message about this event.a
	 */
	private void destroyStar() {
		spawnStarSprite();
		World.sendWorldMessage("<img=5><col=E65D02>The shooting star has turned to dust.", false);
		World.sendWorldMessage("<img=5><col=E65D02>The next one is predicted to appear in 2 hours!", false);
		World.removeObject(star);
		star = null;
	}

	private void spawnStarSprite() {
		final NPC sprite = new NPC(8091, star);
		World.players().filter(p -> p.getActionManager().getAction() instanceof CrashedStarMining).forEach(p -> p.putAttribute("requires_star_reward", true));
		CoresManager.schedule(() -> World.removeNPC(sprite), 3, TimeUnit.MINUTES);
	}

	/**
	 * Gets a list of the junk rewards
	 *
	 * @param player
	 * 		The player
	 */
	public static List<Item> getJunkRewards(Player player) {
		int decentAmount = Utils.random(1, 2);
		int commonAmount = Utils.random(1, 5);
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i <= decentAmount; i++) {
			int itemId = DECENT_REWARDS[Utils.random(DECENT_REWARDS.length)];
			itemList.add(new Item(itemId, itemId == 868 ? Utils.random(20, 200) : itemId == 15273 ? Utils.random(3, 10) : itemId == 10476 ? Utils.random(50, 200) : 1));
		}
		for (int i = 0; i <= commonAmount; i++) {
			int itemId = COMMON_REWARDS[Utils.random(COMMON_REWARDS.length)];
			itemList.add(new Item(itemId, itemId == 868 ? Utils.random(20, 200) : itemId == 15273 ? Utils.random(3, 10) : itemId == 10476 ? Utils.random(50, 200) : 1));
		}
		return itemList;
	}

	/**
	 * The information about the shooting star that is shown in player's information tab
	 */
	public String getStarInformation() {
		if (star != null) {
			return "Shooting Star: <col=" + ChatColors.WHITE + ">" + spawnLocation;
		} else {
			long milliseconds = (lastSpawnedAt + TimeUnit.HOURS.toMillis(2)) - System.currentTimeMillis();
			long mins = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
			return mins + " mins until: <col=" + ChatColors.WHITE + ">Shooting Star</col> ";
		}
	}

	/**
	 * Gets the location that the star was spawned at
	 */
	public String getSpawnLocation() {
		return spawnLocation;
	}

	/**
	 * Verifying that the star exists
	 */
	public boolean starExists() {
		return star != null;
	}

	/**
	 * The star locations
	 *
	 * @author Tyluur
	 */
	private enum StarLocations {

		FALADOR(new WorldTile[] { new WorldTile(2961, 3403, 0) }, "Falador Town Centre"),
		DRAYNOR(new WorldTile[] { new WorldTile(3080, 3250, 0) }, "Draynor Marketplace"),
		CAMELOT(new WorldTile[] { new WorldTile(2681, 3483, 0) }, "Seers' Village"),
		KARAMJA(new WorldTile(2882, 3173, 0), "Karamja"),
		YANILLE(new WorldTile[] { new WorldTile(2603, 3087, 0) }, "Yanille"),
		DUEL_ARENA(new WorldTile(3064, 3498, 0), "Edgeville Monastery"),
		MISCELLANIA(new WorldTile[] { new WorldTile(2527, 3868, 0) }, "Miscellania"),
		MAGE_ARENA(new WorldTile[] { new WorldTile(3104, 3933, 0) }, "Mage Arena"),
		CATHERBY_BANK(new WorldTile(2802, 3448, 0), "Catherby Bank"),
		NEITIZNOT(new WorldTile(2319, 3811, 0), "Neitiznot Yaks"),
		GAMERS_GROTTO(new WorldTile(2986, 9680, 0), "Gamers' Grotto"),
		BARBARIAN_VILLAGE(new WorldTile(3086, 3422, 0), "Barbarian Village"),
		WILDERNESS_CASTLE(new WorldTile[] { new WorldTile(3008, 3636, 0) }, "Dark Warriors' Fortress (Wild)"),
		WILDERNESS_RESOURCE_CENTER(new WorldTile[] { new WorldTile(3031, 3693, 0) }, "Wilderness Resource Center");

		/**
		 * The array of tiles that are applicable to this location
		 */
		private final WorldTile[] tiles;

		/**
		 * The array of names that are applicable to this location
		 */
		private final String[] locationNames;

		StarLocations(WorldTile tile, String name) {
			this.tiles = new WorldTile[] { tile };
			this.locationNames = new String[] { name };
		}

		StarLocations(WorldTile[] tiles, String... locationNames) {
			this.tiles = tiles;
			this.locationNames = locationNames;
		}

		/**
		 * @return the locationNames
		 */
		public String[] getLocationNames() {
			return locationNames;
		}

		/**
		 * @return the tiles
		 */
		public WorldTile[] getTiles() {
			return tiles;
		}
	}

	public enum StarStages {

		FIRST(10, 14, 200, 3),
		SECOND(20, 25, 150, 3),
		THIRD(30, 29, 125, 3),
		FOURTH(40, 32, 100, 4),
		FIFTH(50, 47, 80, 4),
		SIXTH(60, 71, 60, 5),
		SEVENTH(70, 114, 40, 5),
		EIGHTH(80, 145, 30, 6),
		NINTH(90, 210, 15, 7);

		private final int levelRequired, exp, health, delay;

		StarStages(int levelRequired, int exp, int health, int delay) {
			this.levelRequired = levelRequired;
			this.exp = exp;
			this.health = health;
			this.delay = delay;
		}

		/**
		 * @return the health
		 */
		public int getHealth() {
			return health;
		}

		/**
		 * @return the exp
		 */
		public int getExp() {
			return exp;
		}

		/**
		 * @return the levelRequired
		 */
		public int getLevelRequired() {
			return levelRequired;
		}

		/**
		 * @return the delay
		 */
		public int getDelay() {
			return delay;
		}
	}

	public static class ShootingStar extends WorldObject {

		private static final long serialVersionUID = 1L;

		/**
		 * The time the star was spawned
		 */
		private final long spawnedAt;

		/**
		 * If the star has been tagged
		 */
		private boolean beenTagged;

		/**
		 * The health of the star
		 */
		private int health;

		/**
		 * The stage the star is on
		 */
		private StarStages stage;

		/**
		 * If we should remove the star
		 */
		private boolean toRemove = false;

		/**
		 * Constructs a new shooting star
		 *
		 * @param id
		 * 		The id of the star
		 * @param type
		 * 		The type of star this is (object var)
		 * @param rotation
		 * 		The rotation of the star (object var)
		 * @param tile
		 * 		The tile of the star
		 */
		public ShootingStar(int id, int type, int rotation, WorldTile tile) {
			super(id, type, rotation, tile);
			spawnedAt = System.currentTimeMillis();
			setStage(StarStages.FIRST);
			setHealth(stage.getHealth());
		}

		public void updateStage() {
			StarStages nextStage = stage == StarStages.NINTH ? null : StarStages.values()[stage.ordinal() + 1];
			/*World.players().forEach(p -> {
				if (p.getActionManager().getAction() instanceof CrashedStarMining) {
					giveReward(p);
				}
			});*/
			if (nextStage == null) {
				setToRemove(true);
				return;
			}
			setStage(nextStage);
			setHealth(stage.getHealth());
			setId(STAR_IDS[stage.ordinal()]);
			World.spawnObject(this);
		}

		/**
		 * @param toRemove
		 * 		the toRemove to set
		 */
		public void setToRemove(boolean toRemove) {
			this.toRemove = toRemove;
		}

		/**
		 * @param player
		 * 		The player to give the reward to
		 */
		private void giveReward(Player player) {
			int random;
			int itemCount = Utils.random(3);
			for (int count = 0; count < itemCount; count++) {
				random = Utils.random(1, 200);
				boolean rareTable = false;
				if (random <= 3) {
					rareTable = true;
				}
				int itemId;
				if (rareTable) {
					itemId = RARE_REWARDS[Utils.random(RARE_REWARDS.length)];
				} else {
					// common
					if (random <= 50) {
						itemId = COMMON_REWARDS[Utils.random(COMMON_REWARDS.length)];
					} else {
						itemId = DECENT_REWARDS[Utils.random(DECENT_REWARDS.length)];
					}
				}
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, itemId, "You receive a reward from the star!");
				player.getInventory().addItemDrop(itemId, itemId == 15273 ? Utils.random(3, 10) : itemId == 10476 ? Utils.random(50, 200) : 1);
			}
		}

		/**
		 * @return the spawnedAt
		 */
		public long getSpawnedAt() {
			return spawnedAt;
		}

		/**
		 * @return the beenTagged
		 */
		public boolean hasBeenTagged() {
			return beenTagged;
		}

		/**
		 * @param beenTagged
		 * 		the beenTagged to set
		 */
		public void setBeenTagged(boolean beenTagged) {
			this.beenTagged = beenTagged;
		}

		/**
		 * @return the stage
		 */
		public StarStages getStage() {
			return stage;
		}

		/**
		 * @param stage
		 * 		the stage to set
		 */
		public void setStage(StarStages stage) {
			this.stage = stage;
		}

		/**
		 * @return the health
		 */
		public int getHealth() {
			return health;
		}

		/**
		 * @param health
		 * 		the health to set
		 */
		public void setHealth(int health) {
			this.health = health;
		}

		/**
		 * Deducts the health
		 *
		 * @param amount
		 * 		The amount to deduct from the health
		 */
		public void deductHealth(int amount) {
			this.health -= amount;
		}

		/**
		 * @return the toRemove
		 */
		public boolean shouldRemove() {
			return toRemove;
		}
	}

}
