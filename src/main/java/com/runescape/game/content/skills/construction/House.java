package com.runescape.game.content.skills.construction;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.game.content.skills.construction.HouseConstants.Builds;
import com.runescape.game.content.skills.construction.HouseConstants.HObject;
import com.runescape.game.content.skills.construction.HouseConstants.POHLocation;
import com.runescape.game.content.skills.construction.HouseConstants.Room;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.impl.object.WeaponSelection;
import com.runescape.game.interaction.dialogues.impl.skills.CreateRoomD;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.region.DynamicRegion;
import com.runescape.game.world.region.Region;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.game.world.region.RegionUtils;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/*
 * House class only contains house data + support methods to change that data
 * HouseController provides support between player interaction inside house and housemanager
 * HouseConstants handles the constants such as existing rooms, builds, roofs
 */
public class House implements Serializable {

	public static int LOGGED_OUT = 0, KICKED = 1, TELEPORTED = 2;
	private static final long serialVersionUID = 8111719490432901786L;

	// public static final boolean TESTING = true;

	// dont name it rooms or it will null server
	private List<RoomReference> roomsR;

	private byte look;
	private POHLocation location;
	private boolean buildMode;
	private boolean arriveInPortal;

	private transient Player player;
	private transient boolean locked;

	// house loaded datas
	private transient List<Player> players;
	private transient int[] boundChuncks;
	private transient boolean loaded;

	private byte build;

	private boolean isOwnerInside() {
		return players.contains(player);
	}

	public void expelGuests() {
		if (!isOwnerInside()) {
			player.getPackets().sendGameMessage("You can only expel guests when you are in your own house.");
			return;
		}
		kickGuests();
	}

	public void kickGuests() {
		if (players == null)
			return;
		for (Player player : new ArrayList<Player>(players)) {
			if (isOwner(player)) {
				continue;
			}
			leaveHouse(player, KICKED);
		}
	}

	public boolean isOwner(Player player) {
		return this.player == player;
	}

	public void enterMyHouse() {
		joinHouse(player);
	}

	public void openRoomCreationMenu(WorldObject door) {
		int roomX = player.getChunkX() - boundChuncks[0]; // current room
		int roomY = player.getChunkY() - boundChuncks[1]; // current room
		int xInChunk = player.getXInChunk();
		int yInChunk = player.getYInChunk();
		if (xInChunk == 7) {
			roomX += 1;
		} else if (xInChunk == 0) {
			roomX -= 1;
		} else if (yInChunk == 7) {
			roomY += 1;
		} else if (yInChunk == 0) {
			roomY -= 1;
		}
		openRoomCreationMenu(roomX, roomY, door.getPlane());
	}

	public void removeRoom() {
		int roomX = player.getChunkX() - boundChuncks[0]; // current room
		int roomY = player.getChunkY() - boundChuncks[1]; // current room
		RoomReference room = getRoom(roomX, roomY, player.getPlane());
		if (room == null) {
			return;
		}
		if (room.getZ() != 1) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You cannot remove a building that is supporting this room.");
			return;
		}

		RoomReference above = getRoom(roomX, roomY, 2);
		RoomReference below = getRoom(roomX, roomY, 0);

		RoomReference roomTo = above != null && above.getStaircaseSlot() != -1 ? above : below != null && below.getStaircaseSlot() != -1 ? below : null;
		if (roomTo == null) {
			player.getDialogueManager().startDialogue("SimpleMessage", "These stairs do not lead anywhere.");
			return;
		}
		openRoomCreationMenu(roomTo.getX(), roomTo.getY(), roomTo.getZ());
	}

	/*
	 * door used to calculate where player facing to create
	 */
	public void openRoomCreationMenu(int roomX, int roomY, int plane) {
		if (!buildMode) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You can only do that in building mode.");
			return;
		}
		RoomReference room = getRoom(roomX, roomY, plane);
		if (room != null) {
			if (room.plane == 1 && getRoom(roomX, roomY, room.plane + 1) != null) {
				player.getDialogueManager().startDialogue("SimpleMessage", "You can't remove a room that is supporting another room.");
				return;
			}
			if ((room.room == Room.GARDEN || room.room == Room.FORMAL_GARDEN) && getPortalCount() < 2) {
				if (room == getPortalRoom()) {
					player.getDialogueManager().startDialogue("SimpleMessage", "Your house must have at least one exit portal.");
					return;
				}
			}
			player.getDialogueManager().startDialogue("RemoveRoomD", room);
		} else {
			if (roomX == 0 || roomY == 0 || roomX == 7 || roomY == 7) {
				player.getDialogueManager().startDialogue("SimpleMessage", "You can't create a room here.");
				return;
			}
			if (plane == 2) {
				RoomReference r = getRoom(roomX, roomY, 1);
				if (r == null || (r.room == Room.GARDEN || r.room == Room.FORMAL_GARDEN || r.room == Room.MENAGERIE)) {
					player.getDialogueManager().startDialogue("SimpleMessage", "You can't create a room here.");
					return;
				}

			}
			for (int index = 0; index < HouseConstants.Room.values().length; index++) {
				Room refRoom = HouseConstants.Room.values()[index];
				// if (player.getSkills().getLevel(Skills.CONSTRUCTION) >=
				// refRoom.getLevel() && player.getInventory().getCoinsAmount()
				// >= refRoom.getPrice()) {
				boolean canUse = player.getSkills().getLevel(Skills.CONSTRUCTION) >= refRoom.getLevel() && player.getInventory().getCoinsAmount() >= refRoom.getPrice();
				player.getPackets().sendIComponentText(402, index + 68, "<col=" + (canUse ? "008000" : ChatColors.RED) + "> " + refRoom.getPrice() + " coins");
				// }
			}
			player.getInterfaceManager().sendInterface(402);
			player.getAttributes().put("CreationRoom", new int[] { roomX, roomY, plane });
			player.setCloseInterfacesEvent(new Runnable() {
				@Override
				public void run() {
					player.getAttributes().remove("CreationRoom");
				}
			});
		}
	}

	public void climbStaircase(WorldObject object, boolean up) {
		if (object == null || boundChuncks == null) {
			return;
		}
		int roomX = object.getChunkX() - boundChuncks[0];
		int roomY = object.getChunkY() - boundChuncks[1];
		RoomReference room = getRoom(roomX, roomY, object.getPlane());
		if (room == null) {
			return;
		}
		if (room.plane == (up ? 2 : 0)) {
			player.getPackets().sendGameMessage("You are on the " + (up ? "highest" : "lowest") + " possible level so you cannot add a room " + (up ? "above" : "under") + " here.");
			return;
		}
		RoomReference roomTo = getRoom(roomX, roomY, room.plane + (up ? 1 : -1));
		if (roomTo == null) {
			if (buildMode) {
				player.getDialogueManager().startDialogue("CreateRoomStairsD", room, up);
			} else {
				player.getPackets().sendGameMessage("These stairs do not lead anywhere.");
			}
			// start dialogue
			return;
		}
		if (roomTo.getStaircaseSlot() == -1) {
			player.getPackets().sendGameMessage("These stairs do not lead anywhere.");
			return;
		}
		player.useStairs(-1, new WorldTile(player.getX(), player.getY(), player.getPlane() + (up ? 1 : -1)), 0, 1);

	}

	public void removeRoom(RoomReference room) {
		if (roomsR.remove(room)) {
			refreshNumberOfRooms();
			refreshHouse();
		}
	}

	public void createRoom(int slot) {
		Room[] rooms = HouseConstants.Room.values();
		if (slot >= rooms.length) {
			return;
		}
		int[] position = (int[]) player.getAttributes().get("CreationRoom");
		player.closeInterfaces();
		if (position == null) {
			return;
		}
		Room room = rooms[slot];
		if ((room == Room.DUNGEON_CORRIDOR || room == Room.DUNGEON_JUNCTION || room == Room.DUNGEON_PIT || room == Room.DUNGEON_STAIRS) && position[2] != 0) {
			player.getPackets().sendGameMessage("That room can only be built underground.");
			return;
		}
		if ((room == Room.GARDEN || room == Room.FORMAL_GARDEN || room == Room.MENAGERIE) && position[2] != 1) {
			player.getPackets().sendGameMessage("That room can only be built on ground.");
			return;
		}
		if (room.getLevel() > player.getSkills().getLevel(Skills.CONSTRUCTION)) {
			player.getPackets().sendGameMessage("You need a Construction level of " + room.getLevel() + " to build this room.");
			return;
		}
		player.getDialogueManager().startDialogue(CreateRoomD.class, new RoomReference(room, position[0], position[1], position[2], 0));
	}

	public void createRoom(RoomReference room) {
		if (!player.takeMoney(room.room.getPrice())) {
			player.getPackets().sendGameMessage("You don't have enough coins to build this room.");
			return;
		}
		roomsR.add(room);
		refreshNumberOfRooms();
		refreshHouse();
	}

	// Used for inter 396
	private static final int[] BUILD_INDEXES = { 0, 2, 4, 6, 1, 3, 5 };

	public void openBuildInterface(WorldObject object, final Builds build) {
		if (!buildMode) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You can only do that in building mode.");
			return;
		}
		int roomX = object.getChunkX() - boundChuncks[0];
		int roomY = object.getChunkY() - boundChuncks[1];
		RoomReference room = getRoom(roomX, roomY, object.getPlane());
		if (room == null) {
			return;
		}
		int interId = build.getPieces().length > 3 ? 396 : 394;
		Item[] itemArray = new Item[interId == 396 ? 7 : 3];
		for (int index = 0; index < build.getPieces().length; index++) {
			HObject piece = build.getPieces()[index];
			itemArray[interId == 396 ? BUILD_INDEXES[index] : index] = new Item(piece.getItemId(), 1);
			player.getVarsManager().sendVar(1485 + index, player.getSkills().getLevel(Skills.CONSTRUCTION) >= piece.getLevel() && player.getInventory().containsItems(piece.getRequirements()) ? 1 : 0);
		}
		player.getPackets().sendItems(8, itemArray);
		player.getPackets().sendInterSetItemsOptionsScript(interId, 11, 8, interId == 396 ? 2 : 1, 4, "Build");
		player.getPackets().sendUnlockIComponentOptionSlots(interId, 11, 0, interId == 396 ? 7 : 3, 0);
		player.getInterfaceManager().sendInterface(interId);
		for (int i = 0; i < (interId == 396 ? 7 : 3); i++) {
			if (i >= build.getPieces().length) {
				player.getPackets().sendHideIComponent(interId, (interId == 394 ? 29 : 49) + i, true);
				player.getPackets().sendIComponentText(interId, (interId == 394 ? 32 : 56) + i, "");
				player.getPackets().sendIComponentText(interId, 14 + (5 * i), "");
				for (int i2 = 0; i2 < 4; i2++) {
					player.getPackets().sendIComponentText(interId, 15 + i2 + (5 * i), "");
				}
			} else {
				player.getPackets().sendIComponentText(interId, (interId == 394 ? 32 : 56) + i, "Lvl " + build.getPieces()[i].getLevel());
				player.getPackets().sendIComponentText(interId, 14 + (5 * i), ItemDefinitions.forId(build.getPieces()[i].getItemId()).getName());
				for (int i2 = 0; i2 < 4; i2++) {
					player.getPackets().sendIComponentText(interId, 15 + i2 + (5 * i), build.getPieces()[i].getRequirements().length <= i2 ? "" : build.getPieces()[i].getRequirements()[i2].getName() + ": " + build.getPieces()[i].getRequirements()[i2].getAmount());
				}
			}
		}
		player.getAttributes().put("OpenedBuild", build);
		player.getAttributes().put("OpenedBuildObject", object);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getAttributes().remove("OpenedBuild");
				player.getAttributes().remove("OpenedBuildObject");
			}

		});
	}

	public void build(int slot) {
		final Builds build = (Builds) player.getAttributes().get("OpenedBuild");
		WorldObject object = (WorldObject) player.getAttributes().get("OpenedBuildObject");
		if (build == null || object == null || build.getPieces().length <= slot) { return; }
		int roomX = object.getChunkX() - boundChuncks[0];
		int roomY = object.getChunkY() - boundChuncks[1];
		final RoomReference room = getRoom(roomX, roomY, object.getPlane());
		if (room == null) { return; }
		final HObject piece = build.getPieces()[slot];
		if (!hasRequirimentsToBuild(true, build, piece)) { return; }
		final ObjectReference oref = room.addObject(build, slot);
		player.closeInterfaces();
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(build.isWater() ? 2293 : 3683));
		for (Item item : piece.getRequirements()) { player.getInventory().deleteItem(item); }
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getSkills().addXp(Skills.CONSTRUCTION, piece.getXP());
				if (build.isWater()) { player.getSkills().addXp(Skills.FARMING, piece.getXP()); }
				refreshObject(room, oref, false);
				player.getLockManagement().lockAll(1);
			}
		}, 2);
	}

	private boolean hasRequirimentsToBuild(boolean warn, Builds build, HObject piece) {
		int level = player.getSkills().getLevel(Skills.CONSTRUCTION);
		if (!build.isWater() && player.getInventory().containsOneItem(9625)) { level += 3; }
		if (level < piece.getLevel()) {
			if (warn) { player.getPackets().sendGameMessage("Your level of construction is too low for this build."); }
			return false;
		}
		if (!player.getInventory().containsItems(piece.getRequirements())) {
			if (warn) {
				StringBuilder bldr = new StringBuilder();
				for (int i = 0; i < piece.getRequirements().length; i++) {
					Item item = piece.getRequirements()[i];
					bldr.append(item.getAmount() + "x " + item.getName() + "" + (i == piece.getRequirements().length - 1 ? "" : ", "));
				}
				player.getPackets().sendGameMessage("You dont have the right materials. You need: " + bldr.toString() + ".");
			}
			return false;
		}
		if (build.isWater() ? !hasWaterCan() : (!player.getInventory().contains(HouseConstants.HAMMER) || (!player.getInventory().contains(HouseConstants.SAW) && !player.getInventory().containsOneItem(9625)))) {
			if (warn) {
				player.getPackets().sendGameMessage(build.isWater() ? "You will need a watering can with some water in it instead of hammer and saw to build plants." : "You will need a hammer and saw to build furniture.");
			}
			return false;
		}
		return true;
	}

	private void refreshObject(RoomReference rref, ObjectReference oref, boolean remove) {
		int boundX = rref.x * 8;
		int boundY = rref.y * 8;
		int[] regionPos = RegionUtils.convert(RegionUtils.Structure.CHUNK, RegionUtils.Structure.REGION, boundChuncks);
		final Region region = World.getRegion(RegionUtils.encode(RegionUtils.Structure.REGION, regionPos), true);
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				WorldObject[] objects = region.getObjects(rref.plane, boundX + x, boundY + y);
				if (objects != null) {
					for (WorldObject object : objects) {
						if (object == null) {
							continue;
						}
						int slot = oref.build.getIdSlot(object.getId());
						if (slot == -1) {
							continue;
						}
						if (remove) {
							World.spawnObject(object);
						} else {
							WorldObject objectR = new WorldObject(object);
							objectR.setId(oref.getId(slot));
							World.spawnObject(objectR);
						}
					}
				}
			}
		}
	}

	public boolean hasWaterCan() {
		for (int id = 5333; id <= 5340; id++) {
			if (player.getInventory().contains(id)) {
				return true;
			}
		}
		return false;
	}

	public void openRemoveBuild(WorldObject object) {
		if (!buildMode) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You can only do that in building mode.");
			return;
		}
		if (object.getId() == HouseConstants.HObject.EXIT_PORTAL.getId() && getPortalCount() <= 1) {
			player.getDialogueManager().startDialogue("SimpleMessage", "Your house must have at least one exit portal.");
			return;
		}
		int roomX = object.getChunkX() - boundChuncks[0];
		int roomY = object.getChunkY() - boundChuncks[1];
		RoomReference room = getRoom(roomX, roomY, object.getPlane());
		if (room == null) {
			return;
		}
		ObjectReference ref = room.getObject(object);
		if (ref != null) {
			if (ref.build.toString().contains("STAIRCASE")) {
				if (object.getPlane() != 1) {
					RoomReference above = getRoom(roomX, roomY, 2);
					RoomReference below = getRoom(roomX, roomY, 0);
					if ((above != null && above.getStaircaseSlot() != -1) || (below != null && below.getStaircaseSlot() != -1)) {
						player.getDialogueManager().startDialogue("SimpleMessage", "You cannot remove a building that is supporting this room.");
					}
					return;
				}
			}
			player.getDialogueManager().startDialogue("RemoveBuildD", object);
		}
	}

	public void removeBuild(final WorldObject object) {
		if (!buildMode) { // imagine u use settings to change while dialogue
							// open, cheater :p
			player.getDialogueManager().startDialogue("SimpleMessage", "You can only do that in building mode.");
			return;
		}
		int roomX = object.getChunkX() - boundChuncks[0];
		int roomY = object.getChunkY() - boundChuncks[1];
		final RoomReference room = getRoom(roomX, roomY, object.getPlane());
		if (room == null) {
			return;
		}
		final ObjectReference oref = room.removeObject(object);
		if (oref == null) {
			return;
		}
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(3685));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				World.removeObject(object);
				refreshObject(room, oref, true);
				player.getLockManagement().lockAll(1000);
			}
		}, 1);
	}

	public boolean isDoor(WorldObject object) {
		return object.getDefinitions().name.equalsIgnoreCase("Door hotspot");
	}

	public boolean isBuildMode() {
		return buildMode;
	}

	public boolean isDoorSpace(WorldObject object) {
		return object.getDefinitions().name.equalsIgnoreCase("Door space");
	}

	public void switchLock(Player player) {
		if (!isOwner(player)) {
			player.getPackets().sendGameMessage("You can only lock your own house.");
			return;
		}
		locked = !locked;
		if (locked) {
			player.getDialogueManager().startDialogue("SimpleMessage", "Your house is now locked to all visistors.");
		} else if (buildMode) {
			player.getDialogueManager().startDialogue("SimpleMessage", "Visitors will be able to enter your house once you leave building mode.");
		} else {
			player.getDialogueManager().startDialogue("SimpleMessage", "Visistors can now enter your house.");
		}
	}

	public static void enterHouse(Player player, String username) {
		Player owner = World.getPlayerByDisplayName(username);
		if (owner == null || !owner.isRunning() || !player.getFriendsIgnores().isOnline(owner) || owner.getHouse() == null || owner.getHouse().locked) {
			player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
			return;
		}
		owner.getHouse().location = POHLocation.HOME_PORTAL;
		if (owner.getHouse().location == null || !player.withinDistance(owner.getHouse().location.getTile(), 16)) {
			player.getPackets().sendGameMessage("Your house is at " + Utils.formatPlayerNameForDisplay(owner.getHouse().location.name()) + ".");
			return;
		}
		owner.getHouse().joinHouse(player);
	}

	public boolean joinHouse(final Player player) {
		if (!isOwner(player)) { // not owner
			if (!isOwnerInside() || !loaded) {
				player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
				return false;
			}
			if (buildMode) {
				player.getPackets().sendGameMessage("The owner currently has build mode turned on.");
				return false;
			}
		}
		players.add(player);
		sendStartInterface(player);
		player.getControllerManager().startController("HouseControler", this);
		
		if (loaded) {
			teleportPlayer(player);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.getLockManagement().lockAll(1000);
					player.getInterfaceManager().sendWindowPane();
				}
			}, 4);
		} else {
			CoresManager.execute(() -> {
				try {
					boundChuncks = RegionBuilder.findEmptyChunkBound(8, 8);
					createHouse(true);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});
		}
		return true;
	}

	public static void leaveHouse(Player player) {
		Controller controller = player.getControllerManager().getController();
		if (controller == null || !(controller instanceof HouseController)) {
			player.getPackets().sendGameMessage("You're not in a house.");
			return;
		}
		for (int item : WeaponSelection.items) {
			player.removeItemCompletely(item);
		}
		player.getLockManagement().lockAll(2000);
		((HouseController) controller).getHouse().leaveHouse(player, KICKED);
	}

	/*
	 * 0 - logout, 1 kicked/tele outside outside, 2 tele somewhere else
	 */
	public void leaveHouse(Player player, int type) {
		player.getControllerManager().removeController();
		location = POHLocation.HOME_PORTAL;
		if (type == LOGGED_OUT) {
			player.setLocation(location.getTile());
		} else if (type == KICKED) {
			player.useStairs(-1, location.getTile(), 0, 1);
		}
		players.remove(player);
		if (players.size() == 0) {
			destroyHouse();
		}
		for (int item : WeaponSelection.items) {
			player.removeItemCompletely(item);
		}
		if (type != LOGGED_OUT) {
			player.getLockManagement().lockAll(2000);
		}
		player.getAttributes().put("inBoxingArena", false);
		player.setCanPvp(false);
	}

	/*
	 * refers to logout
	 */
	public void finish() {
		kickGuests();
		// no need to leavehouse for owner, controler does that itself
	}

	public void refreshHouse() {
		loaded = false;
		sendStartInterface(player);
		createHouse(false);
	}

	public void sendStartInterface(Player player) {
		player.getLockManagement().lockAll();
		player.getPackets().sendWindowsPane(399, 0);
		player.getMusicsManager().playMusic(454);
		player.getPackets().sendMusicEffect(22);
	}

	public void teleportPlayer(Player player) {
		player.setNextWorldTile(getPortal());
	}

	public WorldTile getPortal() {
		if (boundChuncks == null) {
			throw new RuntimeException("BoundChunks were null so room could not be entered.");
		}
		for (RoomReference room : roomsR) {
			if (room == null) {
				System.err.println("RoomReference 'room' object was null.");
				continue;
			}
			if (room.room == HouseConstants.Room.GARDEN || room.room == HouseConstants.Room.FORMAL_GARDEN) {
				for (ObjectReference o : room.objects) {
					if (o == null) {
						System.err.println("ObjectReference instance was null");
						continue;
					}
					if (o.getPiece() == HouseConstants.HObject.EXIT_PORTAL) {
						return new WorldTile(boundChuncks[0] * 8 + room.x * 8 + 3, boundChuncks[1] * 8 + room.y * 8 + 3, room.plane);
					}
				}
			}
		}
		// shouldnt happen
		int[] xyp = RegionUtils.convert(RegionUtils.Structure.CHUNK, RegionUtils.Structure.TILE, boundChuncks);
		return new WorldTile(xyp[0] + 32, xyp[1] + 32, 0);
	}

	public int getPortalCount() {
		int count = 0;
		for (RoomReference room : roomsR) {
			if (room.room == HouseConstants.Room.GARDEN || room.room == HouseConstants.Room.FORMAL_GARDEN) {
				for (ObjectReference o : room.objects) {
					if (o.getPiece() == HouseConstants.HObject.EXIT_PORTAL) {
						count++;
					}
				}
			}
		}
		return count;
	}

	public RoomReference getPortalRoom() {
		for (RoomReference room : roomsR) {
			if (room.room == HouseConstants.Room.GARDEN || room.room == HouseConstants.Room.FORMAL_GARDEN) {
				for (ObjectReference o : room.objects) {
					if (o.getPiece() == HouseConstants.HObject.EXIT_PORTAL) {
						return room;
					}
				}
			}
		}
		return null;
	}

	public House() {
		buildMode = true;
		roomsR = new ArrayList<RoomReference>();
		addRoom(HouseConstants.Room.GARDEN, 3, 3, 0, 0);
		getRoom(3, 3, 0).addObject(Builds.CENTREPIECE, 0);
	}

	public boolean addRoom(HouseConstants.Room room, int x, int y, int plane, int rotation) {
		return roomsR.add(new RoomReference(room, x, y, plane, rotation));
	}

	/*
	 * temporary
	 */
	private void reset() {
		build = 1;
		buildMode = true;
		roomsR = new ArrayList<RoomReference>();
		addRoom(HouseConstants.Room.GARDEN, 3, 3, 1, 0);
		getRoom(3, 3, 1).addObject(Builds.CENTREPIECE, 0);
	}

	public void init() {
		if (build == 0) {
			reset();
		}
		players = new ArrayList<Player>();
		refreshBuildMode();
		refreshArriveInPortal();
		refreshNumberOfRooms();
	}

	public void refreshNumberOfRooms() {
		player.getPackets().sendGlobalConfig(944, roomsR.size());
	}

	public void setArriveInPortal(boolean arriveInPortal) {
		this.arriveInPortal = arriveInPortal;
		refreshArriveInPortal();
	}
	
	public boolean shouldArriveInPortal() {
		return arriveInPortal;
	}

	public void refreshArriveInPortal() {
		player.getVarsManager().sendVarBit(6450, arriveInPortal ? 1 : 0);
	}

	public void setBuildMode(boolean buildMode) {
		if (this.buildMode == buildMode) {
			return;
		}
		this.buildMode = buildMode;
		if (loaded) {
			expelGuests();
			if (isOwnerInside()) {
				// refreshing if owner not inside
				refreshHouse();
			}
		}
		refreshBuildMode();
	}

	public void refreshBuildMode() {
		player.getVarsManager().sendVarBit(2176, buildMode ? 1 : 0);
	}

	public RoomReference getRoom(int x, int y, int plane) {
		for (RoomReference room : roomsR) {
			if (room.x == x && room.y == y && room.plane == plane) {
				return room;
			}
		}
		return null;
	}

	public boolean isSky(int x, int y, int plane) {
		return buildMode && plane == 2 && getRoom((x / 8) - boundChuncks[0], (y / 8) - boundChuncks[1], plane) == null;
	}

	public void previewRoom(RoomReference reference, boolean remove) {
		int boundX = boundChuncks[0] * 8 + reference.x * 8;
		int boundY = boundChuncks[1] * 8 + reference.y * 8;
		int realChunkX = reference.room.getChunkX();
		int realChunkY = reference.room.getChunkY();
		Region region = World.getRegion(RegionUtils.encode(RegionUtils.Structure.REGION, realChunkX / 8, realChunkY / 8));
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				WorldObject[] objects = region.getObjects(reference.plane, (realChunkX & 0x7) * 8 + x, (realChunkY & 0x7) * 8 + y);
				if (objects != null) {
					for (WorldObject object : objects) {
						if (object == null) {
							continue;
						}
						ObjectDefinitions defs = object.getDefinitions();
						if (defs.containsOption(4, "Build")) {
							WorldObject objectR = new WorldObject(object);
							int[] coords = DynamicRegion.translate(x, y, reference.rotation, defs.sizeX, defs.sizeY, object.getRotation());
							objectR.setLocation(new WorldTile(boundX + coords[0], boundY + coords[1], reference.plane));
							objectR.setRotation((object.getRotation() + reference.rotation) & 0x3);
							if (remove) {
								World.removeObject(objectR);
							} else {
								World.spawnObject(objectR);
							}
						}
					}
				}
			}
		}
	}

	public void destroyHouse() {
		final int[] boundChunksCopy = boundChuncks;
		// this way a new house can be created while current house being
		// destroyed
		loaded = false;
		boundChuncks = null;
		CoresManager.schedule((Runnable) () -> {
			try {
				RegionBuilder.destroyMap(boundChunksCopy[0], boundChunksCopy[1], 8, 8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 1200, TimeUnit.MILLISECONDS);
	}

	private static final int[] DOOR_DIR_X = { -1, 0, 1, 1 };
	private static final int[] DOOR_DIR_Y = { 0, 1, 0, -1 };

	public void createHouse(final boolean tp) {
		Object[][][][] data = new Object[4][8][8][];
		// sets rooms data
		for (RoomReference reference : roomsR) {
			data[reference.plane][reference.x][reference.y] = new Object[] { reference.room.getChunkX(), reference.room.getChunkY(), reference.rotation, reference.room.isShowRoof() };
		}
		// sets roof data
		if (!buildMode) { // construct roof
			for (int x = 1; x < 7; x++) {
				skipY: for (int y = 1; y < 7; y++) {
					for (int plane = 2; plane >= 1; plane--) {
						if (data[plane][x][y] != null) {
							boolean hasRoof = (boolean) data[plane][x][y][3];
							if (hasRoof) {
								byte rotation = (byte) data[plane][x][y][2];
								// TODO find best Roof
								data[plane + 1][x][y] = new Object[] { HouseConstants.Roof.ROOF1.getChunkX(), HouseConstants.Roof.ROOF1.getChunkY(), rotation, true };
								continue skipY;
							}
						}
					}
				}
			}
		}
		// builds data
		for (int plane = 0; plane < data.length; plane++) {
			for (int x = 0; x < data[plane].length; x++) {
				for (int y = 0; y < data[plane][x].length; y++) {
					if (data[plane][x][y] != null) {
						RegionBuilder.copyChunk((int) data[plane][x][y][0] + (look >= 4 ? 8 : 0), (int) data[plane][x][y][1], look & 0x3, boundChuncks[0] + x, boundChuncks[1] + y, plane, (byte) data[plane][x][y][2]);
					} else if ((x == 0 || x == 7 || y == 0 || y == 7) && plane == 1) {
						RegionBuilder.copyChunk(HouseConstants.BLACK[0], HouseConstants.BLACK[1], 0, boundChuncks[0] + x, boundChuncks[1] + y, plane, 0);
					} else if (plane == 1) {
						RegionBuilder.copyChunk(HouseConstants.LAND[0] + (look >= 4 ? 8 : 0), HouseConstants.LAND[1], look & 0x3, boundChuncks[0] + x, boundChuncks[1] + y, plane, 0);
					} else if (plane == 0) {
						RegionBuilder.copyChunk(HouseConstants.DUNGEON[0] + (look >= 4 ? 8 : 0), HouseConstants.DUNGEON[1], look & 0x3, boundChuncks[0] + x, boundChuncks[1] + y, plane, 0);
					} else {
						RegionBuilder.cutChunk(boundChuncks[0] + x, boundChuncks[1] + y, plane);
					}
				}
			}
		}
		int[] regionPos = RegionUtils.convert(RegionUtils.Structure.CHUNK, RegionUtils.Structure.REGION, boundChuncks);
		final Region region = World.getRegion(RegionUtils.encode(RegionUtils.Structure.REGION, regionPos), true);
		List<WorldObject> spawnedObjects = region.getSpawnedObjects();
		if (spawnedObjects != null) {
			for (WorldObject object : spawnedObjects) {
				World.removeObject(object);
			}
		}
		List<WorldObject> removedObjects = region.getRemovedObjects();
		if (removedObjects != null) {
			removedObjects.forEach(World::spawnObject);
		}
		// requires to let the region finish loading
		CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {
			@Override
			public void run() {
				if (region.getLoadMapStage() != 2) {
					return;
				}
				this.cancel();
				for (RoomReference reference : roomsR) {
					int boundX = reference.x * 8;
					int boundY = reference.y * 8;
					for (int x = 0; x < 8; x++) {
						for (int y = 0; y < 8; y++) {
							WorldObject[] objects = region.getObjects(reference.plane, boundX + x, boundY + y);
							if (objects != null) {
								skip: for (WorldObject object : objects) {
									if (object == null) {
										continue;
									}
									if (object.getDefinitions().containsOption(4, "Build")) {
										if (isDoor(object)) {
											if (!buildMode && object.getPlane() == 2 && getRoom(((object.getX() / 8) - boundChuncks[0]) + DOOR_DIR_X[object.getRotation()], ((object.getY() / 8) - boundChuncks[1]) + DOOR_DIR_Y[object.getRotation()], object.getPlane()) == null) {
												WorldObject objectR = new WorldObject(object);
												objectR.setId(HouseConstants.WALL_IDS[look]);
												World.spawnObject(objectR);
												continue;
											}
										} else {
											for (ObjectReference o : reference.objects) {
												if (o == null || o.build == null) {
													continue;
												}
												int slot = o.build.getIdSlot(object.getId());
												if (slot != -1) {
													WorldObject objectR = new WorldObject(object);
													objectR.setId(o.getId(slot));
													World.spawnObject(objectR);
													continue skip;
												}
											}
										}
										if (!buildMode) {
											World.removeObject(object);
										}
									} else if (object.getId() == HouseConstants.WINDOW_SPACE_ID) {
										object = new WorldObject(object);
										object.setId(HouseConstants.WINDOW_IDS[look]);
										World.spawnObject(object);
									} else if (isDoorSpace(object)) {
										World.removeObject(object);
									}
								}
							}
						}
					}
				}
				player.setForceNextMapLoadRefresh(true);
				player.loadMapRegions();
				player.getLockManagement().lockAll(1000);
				player.getInterfaceManager().sendWindowPane();
				if (tp) {
					teleportPlayer(player);
				}
				loaded = true;
			}
		}, 2400, 600);
	}

	public boolean isWindow(int id) {
		return id == 13830;
	}

	public static class ObjectReference implements Serializable {

		/**
		* 
		*/
		private static final long serialVersionUID = -22245200911725426L;
		private int slot;
		private Builds build;

		public ObjectReference(Builds build, int slot) {
			this.build = build;
			this.slot = slot;
		}

		public HObject getPiece() {
			return build.getPieces()[slot];
		}

		public int getId() {
			return build.getPieces()[slot].getId();
		}

		public int[] getIds() {
			return build.getPieces()[slot].getIds();
		}

		public int getId(int slot2) {
			return getIds()[slot2];
		}

	}

	public static class RoomReference implements Serializable {

		private static final long serialVersionUID = 4000732770611956015L;

		public RoomReference(HouseConstants.Room room, int x, int y, int plane, int rotation) {
			this.room = room;
			this.x = (byte) x;
			this.y = (byte) y;
			this.plane = (byte) plane;
			this.rotation = (byte) rotation;
			objects = new ArrayList<ObjectReference>();
		}

		private HouseConstants.Room room;
		private byte x, y, plane, rotation;
		private List<ObjectReference> objects;

		public int getStaircaseSlot() {
			for (ObjectReference object : objects) {
				if (object.build.toString().contains("STAIRCASE")) {
					return object.slot;
				}
			}
			return -1;
		}

		public boolean isStaircaseDown() {
			for (ObjectReference object : objects) {
				if (object.build.toString().contains("STAIRCASE_DOWN")) {
					return true;
				}
			}
			return false;
		}

		/*
		 * x,y inside the room chunk
		 */
		public ObjectReference addObject(Builds build, int slot) {
			ObjectReference ref = new ObjectReference(build, slot);
			objects.add(ref);
			return ref;
		}

		public ObjectReference getObject(WorldObject object) {
			for (ObjectReference o : objects) {
				for (int id : o.getIds()) {
					if (object.getId() == id) {
						return o;
					}
				}
			}
			return null;
		}

		public ObjectReference removeObject(WorldObject object) {
			ObjectReference r = getObject(object);
			if (r != null) {
				objects.remove(r);
				return r;
			}
			return null;
		}

		public void setRotation(int rotation) {
			this.rotation = (byte) rotation;
		}

		public byte getRotation() {
			return rotation;
		}

		public Room getRoom() {
			return room;
		}

		public int getZ() {
			return plane;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

	public void setPlayer(Player player) {
		this.player = player;
		look = 5;
	}

	public Player getPlayer() {
		return player;
	}

	public List<Player> getPlayers() {
		return players;
	}
}