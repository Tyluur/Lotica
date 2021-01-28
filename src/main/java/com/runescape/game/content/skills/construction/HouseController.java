package com.runescape.game.content.skills.construction;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.construction.HouseConstants.Builds;
import com.runescape.game.content.skills.cooking.Cooking;
import com.runescape.game.content.skills.cooking.Cooking.Cookables;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class HouseController extends Controller {

	private House house;

	@Override
	public void start() {
		this.house = (House) getArguments()[0];
		getArguments()[0] = null;
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				player.stopAll();
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					player.setNextWorldTile(house.getPortal());
					player.reset();
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	/**
	 * return process normaly
	 */
	@Override
	public boolean processObjectClick5(WorldObject object) {
		if (GameConstants.DEBUG) {
			System.out.println("Object click 5 " + object.getId());
		}
		if (object.getDefinitions().containsOption(4, "Build")) {
			if (!house.isOwner(player)) {
				player.getPackets().sendGameMessage("You can only do that in your own house.");
				return false;
			}
			if (house.isDoor(object)) {
				house.openRoomCreationMenu(object);
			} else {
				for (final Builds build : HouseConstants.Builds.values()) {
					if (build.containsId(object.getId())) {
						house.openBuildInterface(object, build);
						return false;
					}
				}
			}
		} else if (object.getDefinitions().containsOption(4, "Remove")) {
			if (!house.isOwner(player)) {
				player.getPackets().sendGameMessage("You can only do that in your own house.");
				return false;
			}
			house.openRemoveBuild(object);
		}
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (GameConstants.DEBUG) {
			System.out.println("Object click 1");
		}
		if (object.getDefinitions().getOption(1).equalsIgnoreCase("Pray"))
			return true;
		if (object.getId() == HouseConstants.HObject.EXIT_PORTAL.getId()) {
			house.leaveHouse(player, House.KICKED);
		} else if (object.getId() == HouseConstants.HObject.CLAY_FIREPLACE.getId() || object.getId() == HouseConstants.HObject.STONE_FIREPLACE.getId() || object.getId() == HouseConstants.HObject.MARBLE_FIREPLACE.getId()) {
			player.getPackets().sendGameMessage("Use some logs on the fireplace in order to light it.");
		} else if (object.getId() >= HouseConstants.HObject.CRUDE_WOODEN_CHAIR.getId() && object.getId() <= HouseConstants.HObject.MAHOGANY_ARMCHAIR.getId()) {
			final int chair = object.getId() - HouseConstants.HObject.CRUDE_WOODEN_CHAIR.getId();
			player.getActionManager().setAction(new SitChair(player, chair, object));
		} else if (HouseConstants.Builds.BOOKCASE.containsObject(object)) {
			player.getPackets().sendGameMessage("You search the bookcase but find nothing.");
		} else if (HouseConstants.Builds.STAIRCASE.containsObject(object) || HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			if (object.getDefinitions().getOption(1).equals("Climb")) {
				player.getDialogueManager().startDialogue("ClimbHouseStairD", object);
			} else {
				house.climbStaircase(object, object.getDefinitions().getOption(1).equals("Climb-up"));
			}
		} else if (HouseConstants.Builds.WEAPONS_RACK.containsObject(object)) {
			player.getDialogueManager().startDialogue("WeaponSelection", object);
		} else if (HouseConstants.Builds.COMBAT_RING.containsObject(object)) {
			WorldTile target = null;
			int direction = 0;
			switch (object.getRotation()) {
			case 0:
				if (player.getX() < object.getX()) {
					target = player.transform(1, 0, 0);
					direction = ForceMovement.EAST;
					player.getAttributes().put("inBoxingArena", false);
				} else {
					target = player.transform(-1, 0, 0);
					direction = ForceMovement.WEST;
					player.getAttributes().put("inBoxingArena", true);
				}
				break;
			case 1:
				if (player.getY() <= object.getY()) {
					target = player.transform(0, 1, 0);
					direction = ForceMovement.NORTH;
					player.getAttributes().put("inBoxingArena", true);
				} else {
					target = player.transform(0, -1, 0);
					direction = ForceMovement.SOUTH;
					player.getAttributes().put("inBoxingArena", false);
				}
				break;
			case 2:
				if (player.getX() > object.getX()) {
					target = player.transform(-1, 0, 0);
					direction = ForceMovement.WEST;
					player.getAttributes().put("inBoxingArena", false);
				} else {
					target = player.transform(1, 0, 0);
					direction = ForceMovement.EAST;
					player.getAttributes().put("inBoxingArena", true);
				}
				break;
			case 3:
				if (player.getY() >= object.getY()) {
					target = player.transform(0, -1, 0);
					direction = ForceMovement.SOUTH;
					player.getAttributes().put("inBoxingArena", true);
				} else {
					target = player.transform(0, 1, 0);
					direction = ForceMovement.NORTH;
					player.getAttributes().put("inBoxingArena", false);
				}
				break;
			}
			if (target == null) {
				return false;
			}
			player.getLockManagement().lockAll(1000);
			player.setNextAnimation(new Animation(3688));
			final WorldTile toTile = target;
			player.setNextForceMovement(new ForceMovement(player, 0, toTile, 1, direction));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.setNextWorldTile(toTile);
				}

			}, 0);
		}
		return false;
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		if (interfaceId == 402) {
			if (componentId >= 93 && componentId <= 115)
				player.getHouse().createRoom(componentId - 93);
		} else if (interfaceId == 394 || interfaceId == 396) {
			if (componentId == 11) {
				player.getHouse().build(slotId);
			}
		}
		return true;
	}

	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		if (object.getId() == HouseConstants.HObject.CLAY_FIREPLACE.getId() || object.getId() == HouseConstants.HObject.STONE_FIREPLACE.getId() || object.getId() == HouseConstants.HObject.MARBLE_FIREPLACE.getId()) {
			if (item.getId() != 1511) {
				player.getPackets().sendGameMessage("Only ordinary logs can be used to light the fireplace.");
				return false;
			}
			if (!player.getInventory().contains(590)) {
				player.getPackets().sendGameMessage("You do not have the required items to light this.");
				return false;
			}
			player.getLockManagement().lockAll(2000);
			player.setNextAnimation(new Animation(3658));
			player.getSkills().addXp(Skills.FIREMAKING, 40);
			final WorldObject objectR = new WorldObject(object);
			objectR.setId(object.getId() + 1);
			// wiki says: If you light a fire in your fireplace, then change the
			// graphic settings, the fire will disappear meaning its not realy
			// spawned
			for (final Player player : house.getPlayers()) {
				player.getPackets().sendSpawnedObject(objectR);
			}
			return false;
		} else if (HouseConstants.Builds.STOVE.containsObject(object)) {
			final Cookables cook = Cooking.isCookingSkill(item);
			if (cook != null) {
				player.getDialogueManager().startDialogue("CookingD", cook, object);
				return false;
			}
			player.getDialogueManager().startDialogue("SimpleMessage", "You can't cook that on a " + (object.getDefinitions().name.equals("Fire") ? "fire" : "range") + ".");
			return false;
		}
		return true;
	}

	@Override
	public boolean canDropItem(Item item) {
		if (house.isBuildMode()) {
			player.getPackets().sendGameMessage("You cannot drop items while in building mode.");
			return false;
		}
		return false;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (GameConstants.DEBUG) {
			System.out.println("Object click 2");
		}
		if (object.getId() == HouseConstants.HObject.EXIT_PORTAL.getId()) {
			house.switchLock(player);
		} else if (HouseConstants.Builds.STAIRCASE.containsObject(object) || HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.climbStaircase(object, true);
		}
		return false;
	}

	@Override
	public boolean processObjectClick3(WorldObject object) {
		if (GameConstants.DEBUG) {
			System.out.println("Object click 3");
		}
		if (HouseConstants.Builds.STAIRCASE.containsObject(object) || HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.climbStaircase(object, false);
		}
		return false;
	}

	@Override
	public boolean processObjectClick4(WorldObject object) {
		if (GameConstants.DEBUG) {
			System.out.println("Object click 4");
		}
		if (HouseConstants.Builds.STAIRCASE.containsObject(object) || HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.removeRoom();
		}
		return false;
	}

	@Override
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return !house.isSky(nextX, nextY, player.getPlane());
	}

	@Override
	public boolean logout() {
		house.leaveHouse(player, House.LOGGED_OUT);
		return false; // leave house method removes controller already
	}

	// shouldnt happen but lets imagine somehow in a server restart
	@Override
	public boolean login() {
		player.setNextWorldTile(GameConstants.START_PLAYER_LOCATION);
		removeController();
		return false; // remove controller manualy since i dont want to call
		// forceclose
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.sendMessage("You can't leave the house like this.");
		return false;
	}
	
	@Override
	public void magicTeleported(int type) {
		house.leaveHouse(player, House.TELEPORTED);
	}

	// shouldnt happen
	@Override
	public void forceClose() {
		house.leaveHouse(player, House.TELEPORTED);
	}

	@Override
	public void process() {
		boolean inBoxing = (boolean) player.getAttributes().getOrDefault("inBoxingArena", false);
		player.setCanPvp(inBoxing);
	}

	public House getHouse() {
		return house;
	}

}
