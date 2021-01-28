package com.runescape.game.interaction.controllers.impl.nmz.monster;

import com.runescape.game.interaction.controllers.impl.nmz.NMZController;
import com.runescape.game.interaction.controllers.impl.nmz.NMZInstance;
import com.runescape.game.interaction.controllers.impl.nmz.NMZModes;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/23/2016
 */
public class MonsterGenerator {

	/**
	 * The time before monsters are spawned
	 */
	private static final long SPAWN_DELAY_TIME = TimeUnit.SECONDS.toMillis(10);

	/**
	 * The tiles that monsters can be spawned at
	 */
	private static final int[][] SPAWN_TILES = new int[][] { { 25, 20 }, { 10, 20 }, { 10, 35 }, { 35, 35 }, { 35, 10 }, { 10, 10 } };

	/**
	 * The player who did the most damage to the monster will receive this percent more points after the kill
	 */
	private static final double LEADING_PLAYER_BONUS = 1.25;

	/**
	 * The list of all monsters that must be defeated
	 */
	private final List<NMZMonster> monsters;

	/**
	 * The instance of the game
	 */
	private final NMZInstance game;

	/**
	 * The task that is executed to spawn monsters
	 */
	private Runnable spawnMonstersTask;

	/**
	 * The time the task was created at
	 */
	private long taskCreationTime = -1;

	/**
	 * If we havent spawned the monsters once
	 */
	private boolean spawnedOnce;

	public MonsterGenerator(NMZInstance game) {
		this.monsters = new ArrayList<>();
		this.game = game;
	}

	/**
	 * Processes the system by handling all spawns
	 */
	public void processSystem() {
		//  if we havent spawned monsters yet (the game just started)
		if (!spawnedOnce) {
			// if there is no task set to spawn monsters
			if (spawnMonstersTask == null) {
				spawnMonstersTask = this::spawnFirstWave;
				taskCreationTime = System.currentTimeMillis();
			} else {
				long passed = System.currentTimeMillis() - taskCreationTime;
				if (passed >= SPAWN_DELAY_TIME) {
					spawnMonstersTask.run();
					spawnMonstersTask = null;
				}
			}
		}
	}

	/**
	 * Spawns the first wave of monsters, the amount is different for easy game modes
	 */
	private void spawnFirstWave() {
		//  if we're soloing - only 2 bosses unless we're on elite
		//      otherwise, if we're on easy mode we get 3 or a random number between 3 - [5/8] (hard/elite)
		int amount = game.isSoloing() ? (game.getMode().equals(NMZModes.ELITE) ? 3 : Utils.random(1, 2)) : game.getMode().equals(NMZModes.EASY) ? 3 : Utils.random(3, game.getMode().equals(NMZModes.HARD) ? 5 : 8);
		for (int i = 0; i < amount; i++) {
			spawnReplacementMonster();
		}
		spawnedOnce = true;
	}

	/**
	 * Handles the death of a monster
	 *
	 * @param monster
	 * 		The monster
	 */
	public void handleDeath(NMZMonster monster) {
		givePoints(monster);
		monsters.remove(monster);
		spawnReplacementMonster();
	}

	/**
	 * Gives points to the team for the death of a monster
	 *
	 * @param monster
	 * 		The monster which died
	 */
	private void givePoints(NMZMonster monster) {
		List<Player> playersDealtDamage = monster.playersDealtDamage();

		int basePoints = monster.getBoss().getBasePoints();
		int size = playersDealtDamage.size();
		int divPoints = basePoints / (size <= 0 ? 1 : size);

		Player mostDealtDamage = monster.getMostDamageReceivedSourcePlayer();

		game.getTeam().forEach(player -> player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> {
			int totalPoints = divPoints;
			// if we have more than 1 player in the game and the player has done the most damage
			//  to the monster, they receive a bonus point multiplier
			if (game.getTeam().size() > 1 && mostDealtDamage != null && mostDealtDamage.equals(player)) {
				totalPoints = (int) (totalPoints * LEADING_PLAYER_BONUS);
			}
			totalPoints = (int) (totalPoints * game.getMode().getPointModifier());
			nmz.setPoints(nmz.getPoints() + totalPoints);
		}));
	}

	/**
	 * Spawns a new monster
	 */
	private void spawnReplacementMonster() {
		Object[] array = PossibleBosses.randomBossInstance();
		PossibleBosses boss = (PossibleBosses) array[0];
		monsters.add(boss.createNPC((Integer) array[1], generateSpawnTile(), game));
	}

	/**
	 * Gets a random spawn tile from the {@link #SPAWN_TILES} array
	 */
	private WorldTile generateSpawnTile() {
		int[] array = Utils.randomArraySlot(SPAWN_TILES);
		return game.getWorldTile(array[0], array[1]);
	}

	/**
	 * Gets the list of monsters
	 */
	public List<NMZMonster> getMonsters() {
		return monsters;
	}

}
