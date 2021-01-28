package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.content.global.minigames.pyramids.PyramidFloorFacade.PyramidFloorStage;
import com.runescape.game.content.skills.cooking.Cooking;
import com.runescape.game.content.skills.cooking.Cooking.Cookables;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.skills.CookingD;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.text.DecimalFormat;

public class PyramidHuntingGame extends Controller implements PyramidHunterConstants {

	@Override
	public void start() {
		setFloor((PyramidFloor) getArguments()[0]);
		player.setForceMultiArea(true);
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		return (object.getId() == 6553 || object.getId() == 6555) && !floor.getObjectHandler().checkDoorway(player, object);
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (floor.isInHomeRoom(object)) {
			String name = object.getDefinitions().name.toLowerCase();
			switch (name) {
				case "magic stall":
					int[] primaryRunes = new int[] { 554, 555, 556, 557 };
					int[] combatRunes = new int[] { 558, 562, 560, 565 };
					int amount = Utils.random(6);
					boolean primary = Utils.percentageChance(50);
					int[] arrayToUse = primary ? primaryRunes : combatRunes;
					for (int i = 0; i < amount; i++) {
						int index = Utils.random(arrayToUse.length);
						player.getInventory().addItem(arrayToUse[index], Utils.random(100, 500));
					}
					player.setNextAnimation(new Animation(881));
					player.getLockManagement().lockAll(3000);
					return false;
				case "food stall":
					if (floor.getFacade().getFoodReceived() > 200) {
						player.getDialogueManager().startDialogue(SimpleMessage.class, "You can't steal any more food.");
						return false;
					}
					player.getInventory().addItem(17811, 1);
					player.setNextAnimation(new Animation(881));
					floor.getFacade().setFoodReceived(floor.getFacade().getFoodReceived() + 1);
					player.getLockManagement().lockAll(3000);
					return false;
				default:
					break;
			}
		}
		if (object.equals(floor.getFacade().getTreasure())) {
			floor.handleTreasureSearch(player);
		}
		return true;
	}

	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		Cookables cook = Cooking.isCookingSkill(item);
		if (cook != null && floor.getFacade().getFloorStage() == PyramidFloorStage.PREPARING_ENTRANCE) {
			player.getDialogueManager().startDialogue(CookingD.class, cook, object);
		}
		return true;
	}

	@Override
	public boolean handleItemOnPlayer(Player p2, Item item) {
		if (item.getId() == Cooking.Cookables.RAW_SALVE_EEL.getProduct().getId()) {
			player.getInventory().deleteItem(item);
			p2.applyHit(new Hit(p2, 200, HitLook.HEALED_DAMAGE));
		}
		return true;
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		if (npc.getId() == 8591) {
			switch (floor.getFacade().getFloorStage()) {
				case PREPARING_ENTRANCE:
				case FIGHTING_MONSTERS:
					player.getDialogueManager().startDialogue(primaryInformationDialogue, npc.getId());
					break;
				case SEARCHING_FOR_TREASURE:
					player.getDialogueManager().startDialogue(secondaryInformationDialogue, npc.getId());
					break;
				default:
					break;
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean logout() {
		leave(true);
		return true;
	}

	@Override
	public boolean login() {
		leave(false);
		return true;
	}

	@Override
	public void process() {
		sendInformationInterface();
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 4) {
					player.reset();
					if (lifes-- > 1) {
						player.setNextWorldTile(floor.getWorldTile(33, 36));
						addPoints(-points);
					} else {
						leave(false);
					}
					player.setNextAnimation(new Animation(-1));
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void forceClose() {
		player.getInterfaceManager().closeOverlay();
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		if (interfaceId == 271 && packetId == 61 || (interfaceId == 749 && componentId == 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You are not allowed to use prayers in here.");
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport!");
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport!");
		return false;
	}

	/**
	 * Sends the information interface to the player
	 */
	public void sendInformationInterface() {
		if (floor.getFacade().getFloorStage() == null) {
			return;
		}
		int interfaceId = 532;
		boolean shouldAdd = !player.getInterfaceManager().containsInterface(interfaceId);
		if (shouldAdd) {
			player.getInterfaceManager().sendOverlay(interfaceId);
		}
		StringBuilder bldr = new StringBuilder();
		bldr.append("Pyramid Hunting<br><br>");
		bldr.append("Lifes: " + Utils.format(getLifes()) + "<br>");
		bldr.append("Level: " + floor.getLevel() + "<br>");
		bldr.append("Points: " + Utils.format(getPoints()) + "<br>");
		switch (floor.getFacade().getFloorStage()) {
			case PREPARING_ENTRANCE:
				bldr.append("Food Cooked: " + floor.getFacade().getTotalFoodCooked() + "/" + floor.getFacade().getGoalFoodCooked() + "<br>");
				break;
			case FIGHTING_MONSTERS:
				bldr.append("Monsters Left: " + floor.getFacade().getMonsters().size() + "<br>");
				bldr.append("Damage Dealt: " + Utils.format(damageDealt) + "<br>");
				break;
			case SEARCHING_FOR_TREASURE:
				bldr.append("Hint: " + (floor.getFacade().isShowingHint() ? "<col=" + ChatColors.RED + ">" + floor.getFacade().getHintText() + "</col> from home" : "N/A") + "<br><br>");
				bldr.append("Treasure Left: " + new DecimalFormat("#.##").format(floor.getFacade().getTreasurePercentLeft()) + "%<br>");
				bldr.append("Point Reward (Potential): " + Utils.format(floor.getFacade().getTreasureReward()) + "<br>");
				break;
		}
		player.getPackets().sendIComponentText(interfaceId, 0, "");
		player.getPackets().sendIComponentText(interfaceId, 1, bldr.toString());
	}

	/**
	 * Handling when the player leaves the game
	 *
	 * @param logout
	 * 		If they left by logout
	 */
	public void leave(boolean logout) {
		if (logout) {
			player.setLocation(RESPAWN_LOBBY_COORDINATES);
		} else {
			player.setNextWorldTile(RESPAWN_LOBBY_COORDINATES);
		}
		floor.removePlayer(player);
	}

	/**
	 * Getting the floor which we're in
	 */
	public PyramidFloor getFloor() {
		return floor;
	}

	/**
	 * Setting the floor which we're in
	 *
	 * @param floor
	 * 		The floor
	 */
	public void setFloor(PyramidFloor floor) {
		this.floor = floor;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void addPoints(int points) {
		this.points += points;
	}

	public int getLifes() {
		return lifes;
	}

	public void setLifes(int lifes) {
		this.lifes = lifes;
	}

	public void addDamageDealt(int addition) {
		this.setDamageDealt(this.getDamageDealt() + addition);
	}

	public int getDamageDealt() {
		return damageDealt;
	}

	public void setDamageDealt(int damageDealt) {
		this.damageDealt = damageDealt;
	}

	/**
	 * The amount of lifes the player has
	 */
	private int lifes = 3;

	/**
	 * The amount of points the player has received in their session
	 */
	private int points;

	/**
	 * The amount of damage we've dealt
	 */
	private int damageDealt;

	/**
	 * The current floor we're on
	 */
	private transient PyramidFloor floor;

	private final Dialogue primaryInformationDialogue = new Dialogue() {

		@Override
		public void start() {
			stage = -1;
			npcId = (Integer) parameters[0];
			sendNPCDialogue(npcId, CALM, "Hello, pyramid hunter. How may I help you?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue("Select an Option", "What do I do here?", "Leave, please...");
					stage = 0;
					break;
				case 0:
					switch (option) {
						case FIRST:
							sendNPCDialogue(npcId, CALM, "Aaah, a newcomer. Pyramid Hunting is very simple, but", "complex at the same time! First, cook food from the", "food stall until you've hit the required amount.");
							stage = 3;
							break;
						case SECOND:
							sendNPCDialogue(npcId, CALM, "Are you sure you want to leave?", "You will get NO reward!");
							stage = 1;
							break;
					}
					break;
				case 1:
					sendOptionsDialogue("Are you sure you want to leave?", "Yes", "No");
					stage = 2;
					break;
				case 2:
					switch (option) {
						case FIRST:
							leave(false);
							sendNPCDialogue(npcId, CALM, "Coward, you should have finished...");
							stage = -2;
							break;
						case SECOND:
							end();
							break;
					}
					break;
				case 3:
					sendNPCDialogue(npcId, CALM, "Afterwards, hunt down every monster on your floor", "and kill them. Talk to me when you've completed that.");
					stage = -2;
					break;
			}
		}

		@Override
		public void finish() {

		}

		int npcId;

	};

	private final Dialogue secondaryInformationDialogue = new Dialogue() {

		@Override
		public void start() {
			stage = -1;
			npcId = (Integer) parameters[0];
			sendNPCDialogue(npcId, CALM, "Hey, what can I do for you now?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue("Select an Option", "Request Treasure Hint", "Leave");
					stage = 0;
					break;
				case 0:
					switch (option) {
						case FIRST:
							sendNPCDialogue(npcId, CALM, "I can give you a hint as to where the", "treasure chest is, but everyone in the floor will", "lose " + floor.getPointsToLose() + " points.");
							stage = 3;
							break;
						case SECOND:
							sendNPCDialogue(npcId, CALM, "Are you sure you want to leave?", "You will get NO reward!");
							stage = 1;
							break;
					}
					break;
				case 1:
					sendOptionsDialogue("Are you sure you want to leave?", "Yes", "No");
					stage = 2;
					break;
				case 2:
					switch (option) {
						case FIRST:
							leave(false);
							break;
						case SECOND:
							end();
							break;
					}
					break;
				case 3:
					sendOptionsDialogue("Do you still want a hint?", "Yes, hint please.", "No");
					stage = 4;
					break;
				case 4:
					switch (option) {
						case FIRST:
							floor.showGlobalHint(player);
							break;
						case SECOND:
							break;
					}
					end();
					break;
			}
		}

		@Override
		public void finish() {

		}

		int npcId;

	};
}
