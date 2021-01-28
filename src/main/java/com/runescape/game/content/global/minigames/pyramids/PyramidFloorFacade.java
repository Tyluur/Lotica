package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all of the pyramid floor variables that change per floor.
 * 
 * @author Tyluur<itstyluur@gmail.com>
 * @since February 15, 2015
 *
 */
public final class PyramidFloorFacade {

	public int getTotalFoodCooked() {
		synchronized (LOCK) {
			return totalFoodCooked;
		}
	}

	public void setTotalFoodCooked(int totalFoodCooked) {
		synchronized (LOCK) {
			this.totalFoodCooked = totalFoodCooked;
		}
	}

	public void incrementTotalFoodCooked() {
		synchronized (LOCK) {
			this.totalFoodCooked = this.totalFoodCooked + 1;
		}
	}

	public List<Player> getPlayersFoundTreasure() {
		synchronized (LOCK) {
			return playersFoundTreasure;
		}
	}

	public List<PyramidFloorMonster> getMonsters() {
		synchronized (LOCK) {
			return monsters;
		}
	}

	/**
	 * Gets the floor stage we're on.
	 * 
	 * @return
	 */
	public PyramidFloorStage getFloorStage() {
		return floorStage;
	}

	/**
	 * Setting the current floor stage we're on
	 * 
	 * @param floorStage
	 *            The floor stage
	 */
	public void setFloorStage(PyramidFloorStage floorStage) {
		synchronized (LOCK) {
			this.floorStage = floorStage;
		}
	}

	public int getGoalFoodCooked() {
		return goalFoodCooked;
	}

	public void setGoalFoodCooked(int goalFoodCooked) {
		this.goalFoodCooked = goalFoodCooked;
	}

	public List<WorldTile> getMonsterSpawns() {
		return monsterSpawns;
	}

	public WorldObject getTreasure() {
		return treasure;
	}

	public void setTreasure(WorldObject treasure) {
		this.treasure = treasure;
	}

	public boolean isShowingHint() {
		synchronized (LOCK) {
			return showingHint;
		}
	}

	public void setShowingHint(boolean showingHint) {
		synchronized (LOCK) {
			this.showingHint = showingHint;
		}
	}

	public String getHintText() {
		synchronized (LOCK) {
			return hintText;
		}
	}

	public void setHintText(String hintText) {
		synchronized (LOCK) {
			this.hintText = hintText;
		}
	}

	public double getTreasurePercentLeft() {
		return treasurePercentLeft;
	}

	public void setTreasurePercentLeft(double treasurePercentLeft) {
		synchronized (LOCK) {
			this.treasurePercentLeft = treasurePercentLeft;
		}
	}

	public int getTreasureReward() {
		return treasureReward;
	}

	public void setTreasureReward(int treasureReward) {
		synchronized (LOCK) {
			this.treasureReward = treasureReward;
		}
	}

	public int getFoodReceived() {
		return foodReceived;
	}

	public void setFoodReceived(int foodReceived) {
		synchronized (LOCK) {
			this.foodReceived = foodReceived;
		}
	}

	/**
	 * The current stage the floor is on
	 */
	private PyramidFloorStage floorStage = PyramidFloorStage.PREPARING_ENTRANCE;

	/**
	 * The total amount of food that was cooked
	 */
	private int totalFoodCooked;

	/**
	 * The total amount of food that must be cooked
	 */
	private int goalFoodCooked;

	/**
	 * The amount of food that has been received by all players
	 */
	private int foodReceived;

	/**
	 * If the hint is showing
	 */
	private boolean showingHint;

	/**
	 * The text for the hint
	 */
	private String hintText;

	/**
	 * The world object for the treasure
	 */
	private WorldObject treasure;

	/**
	 * The percent of treasure that is left
	 */
	private double treasurePercentLeft;

	/**
	 * The total reward that players can receive after everyone searches the
	 * chest
	 */
	private int treasureReward;

	/**
	 * The list of players who have found the teasure
	 */
	private final List<Player> playersFoundTreasure = new ArrayList<>();

	/**
	 * The list of monsters in the room
	 */
	private final List<PyramidFloorMonster> monsters = new ArrayList<>();

	/**
	 * The list of monster spawns used
	 */
	private final List<WorldTile> monsterSpawns = new ArrayList<>();

	/**
	 * The object that methods will be synchronized through
	 */
	private static final Object LOCK = new Object();

	public enum PyramidFloorStage {
		PREPARING_ENTRANCE {
			@Override
			public void fireUpdateCheck(PyramidFloor floor) {

			}
		},
		FIGHTING_MONSTERS {
			@Override
			public void fireUpdateCheck(PyramidFloor floor) {
				String message = "All food has been cooked! Start your search for monsters now!";
				for (Player player : floor.getFloorPlayers()) {
					player.stopAll();
					player.getDialogueManager().startDialogue(SimpleMessage.class, message);
					player.sendMessage(message);
				}
				floor.spawnMonsters();
			}
		},
		SEARCHING_FOR_TREASURE {
			@Override
			public void fireUpdateCheck(PyramidFloor floor) {
				floor.spawnTreasure();
				NPC nomad = null;
				for (Player player : floor.getFloorPlayers()) {
					player.restoreAll();
					player.setNextWorldTile(floor.getWorldTile(33, 33));
					nomad = Utils.findLocalNPC(player, 8591);
				}
				if (nomad == null) {
					return;
				}
				nomad.setNextForceTalk(new ForceTalk("Need help finding the treasure? Talk to me!"));
			}
		};

		/**
		 * This method fires the update check for the floor stage
		 * 
		 * @param floor
		 *            The floor we're on
		 */
		public abstract void fireUpdateCheck(PyramidFloor floor);
	}
}
