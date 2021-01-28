package com.runescape.game.interaction.controllers.impl.nmz;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.interaction.controllers.impl.nmz.monster.MonsterGenerator;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.PowerupGenerator;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public class NMZInstance {

	private final long startTime;

	/**
	 * The list of all players in the team
	 */
	private transient final List<Player> team;

	/**
	 * The instance of the monster generator
	 */
	private transient final MonsterGenerator monsterGenerator;

	/**
	 * The powerup calculator
	 */
	private transient final PowerupGenerator powerupGenerator;

	/**
	 * The mode this instance is on
	 */
	private final NMZModes mode;

	/**
	 * The empty region chunks used for generation of the room
	 */
	private final int[] boundChunks;

	/**
	 * Constructs a new nightmare zone instance
	 *
	 * @param team
	 * 		The list of players in the team
	 * @param mode
	 * 		The {@code NMZModes} {@code Object} mode of the instance
	 */
	public NMZInstance(List<Player> team, NMZModes mode) {
		this.team = team;
		this.mode = mode;
		this.boundChunks = RegionBuilder.findEmptyChunkBound(20, 20);
		this.startTime = System.currentTimeMillis();
		this.monsterGenerator = new MonsterGenerator(this);
		this.powerupGenerator = new PowerupGenerator(this);

		initiateTickProcess();
	}

	/**
	 * Initiates the tick process for this nmz instance
	 */
	private void initiateTickProcess() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isGameOver()) {
					shutdown();
					stop();
				} else {
					processGameWork();
				}
			}
		}, 1, 1);
	}

	/**
	 * If the game is over
	 */
	public boolean isGameOver() {
		return team.size() == 0;
	}

	/**
	 * Shuts down the game
	 */
	private void shutdown() {
		//  destroy the dynamic map
		RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
		if (GameConstants.DEBUG) { System.out.println("Shut down the game - its over..."); }
	}

	/**
	 * Processes all game work on a 600ms interval
	 */
	public void processGameWork() {
		if (powerupGenerator.shouldGeneratePowerups()) {
			powerupGenerator.generatePowerups();
		}
		monsterGenerator.processSystem();
	}

	/**
	 * Generates the dynamic map region and delivers every member into the tile
	 */
	public NMZInstance generateAndDeliverRoom() {
		RegionBuilder.copyAllPlanesMap(281, 584, boundChunks[0], boundChunks[1], 64);
		team.forEach(member -> Magic.sendNormalTeleportSpell(member, 0, 0, getWorldTile(25, 9)));
		return this;
	}

	/**
	 * Gets a worldtile in the instance
	 *
	 * @param mapX
	 * 		The x
	 * @param mapY
	 * 		The y
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

	/**
	 * This method will manipulate the player appropriately because they have left. Leaving is by death, logout, or
	 * login (on strange occasions [shouldn't happen])
	 *
	 * @param player
	 * 		The player who died
	 * @param logout
	 * 		If we left by logout
	 */
	public void handlePlayerLeave(Player player, boolean logout) {
		if (logout) {
			player.setLocation(NMZController.LEAVE_TILE);
		} else {
			player.setNextWorldTile(NMZController.LEAVE_TILE);
		}
		player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> {
			int points = nmz.getPoints();

			final int newPoints = getRewardPoints(points);
			if (points > 0) {
				player.getFacade().setDreamPoints(player.getFacade().getDreamPoints() + newPoints);
				player.sendMessage("<col=006600>You have earned " + Utils.format(newPoints) + " reward dream points.");
				player.sendMessage("You wake up feeling refreshed.");
			}
		});
		player.getHintIconsManager().removeUnsavedHintIcon();
		player.getControllerManager().removeController();
		player.getInterfaceManager().closeOverlay();
		removeFromList(player);
	}

	public static int getRewardPoints(int points) {
		int half = (int) (points * 0.50);
		int quarter = (int) (half * 0.50);

		int newPoints = half + quarter;
		newPoints = (int) (newPoints * 1.13);
		newPoints = newPoints + points;

		return newPoints;
	}

	/**
	 * Removes the player from the {@link #team} list of players
	 *
	 * @param player
	 * 		The player
	 */
	private void removeFromList(Player player) {
		Iterator<Player> it = team.iterator();
		while (it.hasNext()) {
			Player p = it.next();
			if (p.equals(player)) {
				it.remove();
				break;
			}
		}
	}

	/**
	 * Gets the time elapsed in the game in a formatted {@code String} (00:00:00)
	 */
	public String getTimeElapsed() {
		long millis = System.currentTimeMillis() - startTime;
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	/**
	 * Gets the mode
	 */
	public NMZModes getMode() {
		return mode;
	}

	/**
	 * Gets the team
	 */
	public List<Player> getTeam() {
		return team;
	}

	/**
	 * Gets the {@link #startTime}
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Gets the {@code PowerupGenerator} instance
	 */
	public PowerupGenerator getPowerupGenerator() {
		return powerupGenerator;
	}

	/**
	 * Gets the {@code MonsterGenerator} instance
	 */
	public MonsterGenerator getMonsterGenerator() {
		return monsterGenerator;
	}

	/**
	 * Adds a minimap marker to the tile
	 *
	 * @param tile
	 * 		The tile
	 */
	public void addMinimapMarker(WorldTile tile) {
		team.forEach(member -> member.getHintIconsManager().addHintIcon(tile.getX(), tile.getY(), tile.getPlane(), 65, 2, 0, -1, false));
	}

	/**
	 * Removes a minimap marker at a tile
	 *
	 * @param tile
	 * 		The tile
	 */
	public void removeMinimapMarker(WorldTile tile) {
		team.forEach(member -> member.getHintIconsManager().removeIconAtTile(tile));
	}

	/**
	 * Gets the middle tile in the map
	 */
	public WorldTile getMiddleTile() {
		return getWorldTile(25, 23);
	}

	/**
	 * If the player left is soloing the fight
	 */
	public boolean isSoloing() {
		return team.size() == 1;
	}

	/**
	 * Removes all hint icon markers
	 */
	public void removeAllMarkers() {
		team.forEach(member -> member.getHintIconsManager().removeUnsavedHintIcon());
	}
}
