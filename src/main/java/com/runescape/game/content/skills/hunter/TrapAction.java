package com.runescape.game.content.skills.hunter;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.OwnedObjectManager;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.region.Region;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class TrapAction extends Action {

	private Traps trap;

	private WorldTile tile;

	public TrapAction(Traps trap, WorldTile tile) {
		this.trap = trap;
		this.tile = tile;
	}

	public static boolean isTrap(Player player, WorldTile tile, int id) {
		for (Traps trap : Traps.values()) {
			if (trap.getIds()[0] != id) { continue; }
			player.getActionManager().setAction(new TrapAction(trap, tile));
			return true;
		}
		return false;
	}

	public static boolean isTrap(Player player, WorldObject o) {
		Traps trap = null;
		for (Traps t : Traps.values()) {
			if ((t.isItem() && (o.getId() == t.getIds()[1] || o.getId() == t.getIds()[2])) || (!t.isItem() && (o.getId() == t.getIds()[2] || o.getId() == t.getIds()[1]))) {
				trap = t;
				break;
			}
		}
		HunterNPC captured = null;
		if (trap == null) {
			for (HunterNPC npc : HunterNPC.values()) {
				if (o.getId() == npc.getIds()[0]) {
					captured = npc;
					trap = captured.trap;
					break;
				}
			}
		}
		if (trap == null) { return false; } else if (!OwnedObjectManager.isPlayerObject(player, o)) {
			player.getPackets().sendGameMessage("This isn't your trap!");
			return true;
		}
		sendTrapAction(player, o, trap, captured);
		return true;
	}

	private static void sendTrapAction(final Player player, final WorldObject o, final Traps trap, final HunterNPC captured) {
		if (player.getLockManagement().isAnyLocked()) { return; }
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(trap.getIds()[trap.isItem() ? 4 : 6]));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getLockManagement().unlockAll();
				if (!World.containsObjectWithId(o.getId(), o)) {
					return;
				}
				if (trap.isItem()) { player.getInventory().addItemDrop(trap.getIds()[0], 1); } else {
					World.spawnObject(new WorldObject(trap.getIds()[0], o.getType(), o.getRotation(), new WorldTile(o.getTileHash())));
				}
				if (captured != null) {
					int[] ids = captured.getIds();
					for (int i = 3; i < ids.length; i++) { player.getInventory().addItemDrop(ids[i], 1); }
					player.getSkills().addXp(Skills.HUNTER, captured.getExp());
					if (player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
						player.getInventory().addItemDrop(Wilderness.WILDERNESS_TOKEN, 3);
					}
				}
				player.getPackets().sendGameMessage(captured != null ? "You've caught a " + Utils.formatPlayerNameForDisplay(captured.toString()) + "." : "You dismantle the trap.");
				OwnedObjectManager.removeObject(player, o);
			}
		}, 2);
	}

	@Override
	public boolean start(Player player) {
		boolean is_item = trap.isItem();
		int levelRequirement = trap.getRequirementLevel(), currentLevel = player.getSkills().getLevel(Skills.HUNTER);
		if (currentLevel < levelRequirement) {
			player.getPackets().sendGameMessage("You need a Hunter level of " + levelRequirement + " in order to place this trap.");
			return false;
		} else {
			if (is_item) {
				if (World.getObjectWithSlot(tile, Region.OBJECT_SLOT_FLOOR) != null) {
					player.getPackets().sendGameMessage("You cannot place a trap here!");
					return false;
				}
			} else {
				int[] ids = trap.getIds();
				Item item = new Item(ids[3], ids[4]);
				if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
					player.getPackets().sendGameMessage("You don't have the neccessary supplies to place this trap.");
					return false;
				}
			}
			int maxAmount = getMaximumTrap(trap, currentLevel);
			if (getTrapsCount(player, is_item) == maxAmount) {
				player.getPackets().sendGameMessage("You cannot place more than " + maxAmount + " traps at once.");
				return false;
			}
		}
		player.getLockManagement().lockAll(3);
		player.setNextAnimation(new Animation(trap.getIds()[is_item ? 3 : 5]));
		player.getPackets().sendGameMessage("You begin setting up the trap.");
		if (is_item) { World.addGroundItem(new Item(trap.getIds()[0], 1), tile, player, true, 180); }
		player.getInventory().deleteItem(is_item ? trap.getIds()[0] : trap.getIds()[3], is_item ? 1 : trap.getIds()[4]);
		setActionDelay(player, 4);
		return true;
	}

	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		boolean is_item = trap.isItem();
		int[] ids = trap.getIds();
		if (is_item) {
			if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1)) {
				if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1)) {
					if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1)) {
						player.addWalkSteps(player.getX(), player.getY() - 1, 1);
					}
				}
			}
			final FloorItem item = World.getRegion(tile.getRegionId()).getFloorItem(ids[0], tile, player);
			if (item == null) { return -1; } else if (!World.removeGroundItem(player, item, false)) { return -1; }
		}
		OwnedObjectManager.addOwnedObjectManager(player, new WorldObject[] { new WorldObject(ids[1], 10, 0, tile.getX(), tile.getY(), tile.getPlane()) }, new long[] { 300000 });
		return -1;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

	private static int getMaximumTrap(Traps trap, int currentLevel) {
		if (trap.isItem()) { return 1 + (currentLevel / 20); }
		return 3;
	}

	private static int getTrapsCount(Player player, boolean item) {
		int trapsCount = 0;
		for (Traps t : Traps.values()) {
			if (t.isItem() != item) { continue; }
			if (item) {
				trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[1]);
				trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[2]);
			} else { trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[0]); }
		}
		for (HunterNPC npc : HunterNPC.values()) {
			if (npc.getTrap().isItem() != item) { continue; }
			trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[0]);
			trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[1]);
			trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[2]);
		}
		return trapsCount;
	}

	public enum Traps {

		/*itemid, objectid, fail obj id, set emote, remove emote*/
		BOX(new int[] { 10008, 19187, 19192, 5208, 5208 }, 27),

		SNARE(new int[] { 10006, 19175, 19174, 5208, 5207 }, 1),

		/*obj id, trans id, fail obj id, itemid, itemamount, set emote, remove emote*/

		BOULDER_TRAP(new int[] { 19205, 19206, 19219, 1511, 1, 5208, 5208 }, 23);

		/*PITFALL(new int[]
		{ 0, 0, 0, 0, 0 }, 31)*/

		private final int[] ids;

		private int requirementLevel;

		Traps(int[] ids, int requirementLevel) {
			this.ids = ids;
			this.requirementLevel = requirementLevel;
		}

		public int[] getIds() {
			return ids;
		}

		public int getRequirementLevel() {
			return requirementLevel;
		}

		public boolean isItem() {
			return ids.length == 5;
		}
	}

	public enum HunterNPC {

		BARB_TAILED_KEBBIT(Traps.BOULDER_TRAP, 23, 168, new int[] { 19215, 5275, 5277, 526, 10129 }),

		GREY_CHINCHOMPA(Traps.BOX, 53, 198.4, new int[] { 28557, 5184, -1, 10033 }),

		RED_CHINCHOMPA(Traps.BOX, 63, 265, new int[] { 28558, 5184, -1, 10034 }),

		FERRET(Traps.BOX, 27, 115, new int[] { 19189, 5191, 5192 }),

		GECKO(Traps.BOX, 27, 100, new int[] { 19190, 8362, 8361 }),

		RACCOON(Traps.BOX, 27, 100, new int[] { 19191, 7726, 7727 }),

		MONKEY(Traps.BOX, 27, 100, new int[] { 28557, 8343, 8345 }),

		CRIMSON_SWIFT(Traps.SNARE, 1, 34, new int[] { 19180, 5171, 5172, 10088, 526, 9978 }),

		GOLDEN_WARBLER(Traps.SNARE, 5, 48, new int[] { 19184, 5171, 5172, 1583, 526, 9978 }),

		COPPER_LONGTAIL(Traps.SNARE, 9, 61, new int[] { 19186, 5171, 5172, 10091, 526, 9978 }),

		CERULEAN_TWITCH(Traps.SNARE, 11, 64.67, new int[] { 19182, 5171, 5172, 10089, 526, 9978 }),

		TROPICAL_WAGTAIL(Traps.SNARE, 19, 95.2, new int[] { 19178, 5171, 5172, 10087, 526, 9978 }),

		WIMPY_BIRD(Traps.SNARE, 39, 167, new int[] { 28930, 5171, 5172, 11525, 526, 9978 });

		private final Traps trap;

		private final int lureLevel;

		private final double exp;

		private final int[] ids;

		HunterNPC(Traps trap, int lureLevel, double exp, int[] ids) {
			this.trap = trap;
			this.lureLevel = lureLevel;
			this.exp = exp;
			this.ids = ids;
		}

		public Traps getTrap() {
			return trap;
		}

		public int getLureLevel() {
			return lureLevel;
		}

		public double getExp() {
			return exp;
		}

		public int[] getIds() {
			return ids;
		}
	}
}