package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.global.minigames.pyramids.PyramidFloorFacade.PyramidFloorStage;
import com.runescape.game.content.global.minigames.pyramids.PyramidFloorMonster.Monsters;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class handles the pyramid floor generation
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since February 15th, 2015
 */
public class PyramidFloor implements PyramidHunterConstants {

	/**
	 * Constructs a new {@link PyramidFloor} {@code Object}
	 */
	public PyramidFloor() {
		this.boundChunks = RegionBuilder.findEmptyChunkBound(20, 20);
		this.facade = new PyramidFloorFacade();
		this.objectHandler = new PyramidObjects(this);
	}

	/**
	 * Starts single player mode floor. The only player in the floor will be the player entering.
	 *
	 * @param player
	 * 		The player entering.
	 */
	public void startSingleMode(Player player) {
		List<Player> floorPlayers = new ArrayList<>();
		floorPlayers.add(player);
		this.floorPlayers = floorPlayers;
		enterFloor(LobbyType.SINGLE);
	}

	/**
	 * Starts the team mode floor. The {@link #floorPlayers} is set to the floorPlayers parameterized. All players then
	 * start the {@link PyramidHuntingGame} controller
	 */
	public void startTeamMode(List<Player> floorPlayers) {
		this.floorPlayers = floorPlayers;
		enterFloor(LobbyType.TEAM);
	}

	/**
	 * This method brings every {@link #floorPlayers} {@code Player} into the pyramid floor.
	 *
	 * @param lobbyType
	 * 		The type of lobby the players were waiting in
	 */
	public void enterFloor(LobbyType lobbyType) {
		setLobbyType(lobbyType);
		facade.setGoalFoodCooked(PyramidHunterConstants.getFoodPerLevel(lobbyType, getLevel()));
		RegionBuilder.copyAllPlanesMap(404, 1164, boundChunks[0], boundChunks[1], 64);
		RegionBuilder.copyAllPlanesMap(400, 1160, boundChunks[0], boundChunks[1], 64);
		for (Player player : floorPlayers) {
			player.setNextWorldTile(getWorldTile(33, 36));
			player.getControllerManager().startController("PHGame", this);
		}
		spawnEverything();
	}

	/**
	 * This method spawns monsters on the floor
	 */
	public void spawnMonsters() {
		int total = PyramidHunterConstants.getRandomMonsterCount(getLevel()) + BASE_SCARAB_COUNT;
		PyramidFloorMonster[] monsters = new PyramidFloorMonster[total];
		for (int i = 0; i < BASE_SCARAB_COUNT; i++) {
			monsters[i] = new PyramidFloorMonster(this, Monsters.SCARAB, getRandomTile(false));
		}
		total = total - BASE_SCARAB_COUNT;
		int zombiesCount = Math.floorDiv(total, 3) * 2;
		int generalCount = total - zombiesCount;
		int zombiesSpawned = 0;
		int generalSpawned = 0;
		for (int i = 0; i < monsters.length; i++) {
			if (monsters[i] != null) {
				continue;
			}
			if (zombiesSpawned < zombiesCount) {
				Monsters monsterType = Monsters.ZOMBIES;

				int id = monsterType.getRandomId();
				WorldTile tile = getRandomTile(true);
				int count = 0;
				while (!World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), NPCDefinitions.getNPCDefinitions(id).size)) {
					if (count == MONSTER_SPAWNS.length) {
						System.out.println("Gave up checking for better spawns...[" + monsterType + ", " + NPCDefinitions.getNPCDefinitions(id).getName() + "]");
						break;
					}
					tile = getRandomTile(true);
					count++;
				}
				monsters[i] = new PyramidFloorMonster(this, id, tile, monsterType);

				zombiesSpawned++;
			} else if (generalSpawned < generalCount) {
				Monsters monsterType = Monsters.GENERAL;

				int id = monsterType.getRandomId();
				WorldTile tile = getRandomTile(true);
				int count = 0;
				while (!World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), NPCDefinitions.getNPCDefinitions(id).size)) {
					if (count == MONSTER_SPAWNS.length) {
						System.out.println("Gave up checking for better spawns...[" + monsterType + ", " + NPCDefinitions.getNPCDefinitions(id).getName() + "]");
						break;
					}
					tile = getRandomTile(true);
				}
				monsters[i] = new PyramidFloorMonster(this, id, tile, monsterType);

				generalSpawned++;
			}
		}
		for (PyramidFloorMonster monster : monsters) {
			facade.getMonsters().add(monster);
		}
	}

	/**
	 * This method spawns different entities in the dynamic region
	 */
	public void spawnEverything() {
		/** Spawning two fires */
		World.spawnObject(new WorldObject(2732, 10, 1, getWorldTile(34, 31)));

		// This fire is ontop of the altar
		World.spawnObject(new WorldObject(2732, 10, 1, getWorldTile(33, 31)));
		World.spawnObject(new WorldObject(2732, 10, 1, getWorldTile(32, 31)));

		/* Spawning the two stalls */
		World.spawnObject(new WorldObject(4875, 10, 1, getWorldTile(33, 34))); // magic

		// removing the portal
		World.spawnObject(new WorldObject(-1, 10, 0, getWorldTile(33, 32)));

		// Nomad
		NPC nomad = new NPC(8591, getWorldTile(33, 32), -1, true);
		nomad.setWalkType(NPC.NO_WALK);
	}

	/**
	 * Checks if the monsters are all killed (by checking that the list is empty), and if they are, we progress to the
	 * {@link PyramidFloorStage#SEARCHING_FOR_TREASURE}
	 */
	private void checkMonsters() {
		if (facade.getMonsters().isEmpty()) {
			facade.setFloorStage(PyramidFloorStage.SEARCHING_FOR_TREASURE);
			fireStageUpdateCheck();
		}
	}

	/**
	 * Spawns the treasure at a random coordinate
	 */
	public void spawnTreasure() {
		WorldTile randomTile = TREASURE_SPAWNS[Utils.random(TREASURE_SPAWNS.length)];
		WorldTile localTile = getWorldTile(randomTile.getX(), randomTile.getY());
		facade.setTreasure(new WorldObject(TREASURE_CHEST_ID, 10, 1, localTile));
		facade.setTreasurePercentLeft(100);
		CoresManager.FAST_EXECUTOR.scheduleAtFixedRate(setTimer(new TreasureTimerTask(this)), 0, TimeUnit.SECONDS.toMillis(1));
		World.spawnObject(facade.getTreasure());

		int totalPoints = 0;
		for (Player pl : floorPlayers) {
			try {
				totalPoints += pl.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).get().getPoints();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		facade.setTreasureReward(Math.floorDiv(totalPoints, 2));
	}

	/**
	 * This method removes the floor from the server
	 */
	public void removeFloor() {
		RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
	}

	/**
	 * Grabs a random {@code WorldTile} {@code Object} from the {@link PyramidHunterConstants#MONSTER_SPAWNS} {@code
	 * Array} of spawns.
	 */
	public WorldTile getRandomTile(boolean checkDuplicates) {
		if (checkDuplicates && facade.getMonsters().size() == MONSTER_SPAWNS.length) {
			facade.getMonsters().clear();
		}
		WorldTile tile = MONSTER_SPAWNS[Utils.random(MONSTER_SPAWNS.length)];
		while (checkDuplicates && facade.getMonsterSpawns().contains(tile)) {
			tile = MONSTER_SPAWNS[Utils.random(MONSTER_SPAWNS.length)];
		}
		tile = getWorldTile(tile.getX(), tile.getY());
		if (checkDuplicates) {
			facade.getMonsterSpawns().add(tile);
		}
		return tile;
	}

	/**
	 * Sets the hint for everybody to see and turns the showing hint variable to true
	 *
	 * @param requested
	 * 		The player who requested the hint
	 */
	public void showGlobalHint(Player requested) {
		WorldTile startTile = getWorldTile(33, 36);
		WorldTile treasureTile = facade.getTreasure().getWorldTile();
		String hint = Utils.getTileInformations(startTile, treasureTile);
		facade.setHintText(hint);

		int pointsLost = getPointsToLose();

		for (Player player : floorPlayers) {
			player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).ifPresent(c -> c.addPoints(-pointsLost));
			player.sendMessage(requested.getDisplayName() + " requested a hint so you lose " + pointsLost + " points!");
		}
		facade.setShowingHint(true);
	}

	/**
	 * The amount of points everyone loses when the hint is displayed
	 */
	public int getPointsToLose() {
		int points = 0;
		for (Player player : floorPlayers) {
			try {
				points += player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).get().getPoints();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Math.floorDiv(points, 3);
	}

	/**
	 * When players cooked food, the total number of food they've cooked is incremented in {@link
	 * PyramidFloorFacade#getTotalFoodCooked()}. When this value reaches the goal value set in {@link
	 * PyramidFloorFacade#getGoalFoodCooked()}, we update to the killing monsters stage.
	 */
	public void checkFoodCooked() {
		if (facade.getFloorStage() != PyramidFloorStage.PREPARING_ENTRANCE) {
			return;
		}
		if (facade.getTotalFoodCooked() >= facade.getGoalFoodCooked()) {
			facade.setFloorStage(PyramidFloorStage.FIGHTING_MONSTERS);
			fireStageUpdateCheck();
		}
	}

	/**
	 * Upon stage update, each stage has custom events that are fired. This method fires those events.
	 */
	private void fireStageUpdateCheck() {
		facade.getFloorStage().fireUpdateCheck(this);
	}

	/**
	 * If the world tile is inside the home room
	 *
	 * @param worldTile
	 * 		The {@link WorldTile} {@code Object} we're checking for
	 */
	public boolean isInHomeRoom(WorldTile worldTile) {
		WorldTile topLeft = new WorldTile(26, 41);
		WorldTile bottomRight = new WorldTile(40, 29);

		WorldTile regionLeft = getWorldTile(topLeft.getX(), topLeft.getY());
		WorldTile regionRight = getWorldTile(bottomRight.getX(), bottomRight.getY());

		boolean correctX = worldTile.getX() >= regionLeft.getX() && worldTile.getX() <= regionRight.getX();
		boolean correctY = worldTile.getY() >= regionRight.getY() && worldTile.getY() <= regionLeft.getY();

		return correctX && correctY;
	}

	/**
	 * Gets the world tile inside the dynamic region
	 *
	 * @param mapX
	 * 		The x in the map
	 * @param mapY
	 * 		The y in the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

	/**
	 * This method removes the player from the {@link #floorPlayers} list
	 *
	 * @param player
	 * 		The player to remove.
	 */
	public void removePlayer(Player player) {
		floorPlayers.remove(player);
		if (floorPlayers.size() <= 0) {
			removeFloor();
		}
		giveRewards(player);
		player.getInventory().deleteItem(18173, Integer.MAX_VALUE);
		player.getInventory().deleteItem(17811, Integer.MAX_VALUE);
		player.getControllerManager().forceStop();
	}

	/**
	 * Gives the player their rewards for game progress.
	 *
	 * @param player
	 * 		The player
	 */
	private void giveRewards(Player player) {
		PyramidHuntingGame game = null;
		try {
			game = player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).orElse(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (game == null) {
			return;
		}
		int pointsReceived = PyramidHunterConstants.getPointsToGive(getLevel(), game.getPoints(), game.getDamageDealt());
		String[] taunts = new String[] { "Perhaps you should've tried harder...", "That's all you've got? I doubt it.", "Where has your talent gone, fair traveller?" };
		player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 8591, "" + taunts[Utils.random(taunts.length)], "You earned " + Utils.format(pointsReceived) + " " + GameConstants.SERVER_NAME + " points though.", "Try again soon.");
		player.getFacade().setDreamPoints(player.getFacade().getDreamPoints() + pointsReceived);
	}

	/**
	 * This method removes the monster from the {@link PyramidFloorFacade#getMonsters()} list
	 *
	 * @param monster
	case "Construction Shop 1":
	 * 		The monster to remove
	 */
	public void removeMonster(PyramidFloorMonster monster) {
		facade.getMonsters().remove(monster);
		checkMonsters();
	}

	/**
	 * This method handles when the player interacts with the treasure
	 *
	 * @param player
	 * 		The player
	 */
	public void handleTreasureSearch(Player player) {
		if (!facade.getFloorStage().equals(PyramidFloorStage.SEARCHING_FOR_TREASURE)) {
			return;
		}
		List<Player> playersLeft = floorPlayers.stream().filter(pl -> !facade.getPlayersFoundTreasure().contains(pl)).collect(Collectors.toList());
		if (facade.getPlayersFoundTreasure().contains(player)) {
			StringBuilder bldr = new StringBuilder();
			for (int i = 0; i < playersLeft.size(); i++) {
				Player pl = playersLeft.get(i);
				bldr.append(pl.getDisplayName()).append("").append(i == (playersLeft.size() - 1) ? "" : ",");
			}
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You have already searched the chest!", "Players waiting on:", bldr.toString());
			return;
		}
		boolean first = facade.getPlayersFoundTreasure().isEmpty() && lobbyType == LobbyType.TEAM;

		boolean receivesBoost = false;
		if (first && !facade.isShowingHint()) {
			receivesBoost = true;
		}
		if (receivesBoost) {
			int pointBoost = Math.floorDiv(player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).get().getPoints(), 5);
			player.sendMessage("You were the first to reach the treasure and no hint was needed!");
			player.sendMessage("You receive a " + pointBoost + " total point boost.");
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You were the first to reach the treasure and no hint was needed!", "You receive a " + pointBoost + " total point boost.");
			try {
				player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).ifPresent(c -> c.addPoints(pointBoost));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!facade.getPlayersFoundTreasure().contains(player)) {
			facade.getPlayersFoundTreasure().add(player);
		}
		if (facade.getPlayersFoundTreasure().size() >= floorPlayers.size()) {
			finishLevel();
		} else {
			player.sendMessage("You have searched the chest... Waiting on " + (playersLeft.size() - 1) + " players now.");
		}
	}

	/**
	 * When this method is called, the level is finished. Everything resets and we restart.
	 */
	private void finishLevel() {
		World.removeObject(facade.getTreasure());
		double percent = facade.getTreasurePercentLeft();
		int pointsPossible = facade.getTreasureReward();
		int pointReward = (int) (pointsPossible * percent / (double) 100);
		for (Player player : floorPlayers) {
			player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).ifPresent(c -> c.addPoints(pointReward));
			player.getDialogueManager().startDialogue(SimpleMessage.class, "Level Complete! You receive " + Utils.format(pointReward) + " reward points!");
		}
		facade = new PyramidFloorFacade();
		facade.setGoalFoodCooked(PyramidHunterConstants.getFoodPerLevel(lobbyType, getLevel()));
		for (Player player : floorPlayers) {
			player.setNextWorldTile(getWorldTile(33, 36));
		}
		setLevel(getLevel() + 1);
		timer.cancel();
	}

	/**
	 * Gets the {@link #floorPlayers} list
	 *
	 * @return A {@code List} {@code Object}
	 */
	public List<Player> getFloorPlayers() {
		return floorPlayers;
	}

	/**
	 * Setting the list of floor players to a new list
	 *
	 * @param floorPlayers
	 * 		A {@code List} of floor {@code Player}s
	 */
	public void setFloorPlayers(List<Player> floorPlayers) {
		this.floorPlayers = floorPlayers;
	}

	public PyramidFloorFacade getFacade() {
		return facade;
	}

	public PyramidObjects getObjectHandler() {
		return objectHandler;
	}

	public LobbyType getLobbyType() {
		return lobbyType;
	}

	public void setLobbyType(LobbyType lobbyType) {
		this.lobbyType = lobbyType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public TreasureTimerTask getTimer() {
		return timer;
	}

	public TreasureTimerTask setTimer(TreasureTimerTask timer) {
		this.timer = timer;
		return timer;
	}

	/**
	 * The list of players in the floor
	 */
	private transient List<Player> floorPlayers;

	/**
	 * The level we're on in the floor
	 */
	private transient int level = 1;

	/**
	 * The chunks for the dynamic region in the floor
	 */
	private transient int[] boundChunks;

	/**
	 * The type of lobby the players were waiting in before the game.
	 */
	private transient LobbyType lobbyType;

	/**
	 * The timer object
	 */
	private transient TreasureTimerTask timer;

	/**
	 * The facade for our floor
	 */
	private transient PyramidFloorFacade facade;

	/**
	 * The instance of the object handler
	 */
	private transient final PyramidObjects objectHandler;

}