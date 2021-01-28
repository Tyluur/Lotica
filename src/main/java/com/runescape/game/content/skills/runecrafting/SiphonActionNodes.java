package com.runescape.game.content.skills.runecrafting;

import com.runescape.game.interaction.controllers.impl.RunespanControler;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class SiphonActionNodes extends Action {

	private Nodes nodes;

	private WorldObject node;

	private boolean started;

	private static final WorldTile[][] NODE_TILES = { { new WorldTile(4337, 6076, 1),
	                                                    new WorldTile(4327, 6067, 1),
	                                                    new WorldTile(4338, 6062, 1),
	                                                    new WorldTile(4359, 6117, 1),
	                                                    new WorldTile(4379, 6058, 1),
	                                                    new WorldTile(4385, 6089, 1),
	                                                    new WorldTile(4322, 6091, 1),
	                                                    new WorldTile(4410, 6073, 1) },

	                                                  { new WorldTile(3938, 6126, 1),
	                                                    new WorldTile(3981, 6139, 1),
	                                                    new WorldTile(3958, 6140, 1),
	                                                    new WorldTile(3931, 6139, 1),
	                                                    new WorldTile(3919, 6133, 1),
	                                                    new WorldTile(3912, 6082, 1),
	                                                    new WorldTile(3953, 6066, 1),
	                                                    new WorldTile(3982, 6064, 1),
	                                                    new WorldTile(4018, 6071, 1),
	                                                    new WorldTile(4136, 6136, 1),
	                                                    new WorldTile(4150, 6134, 1),
	                                                    new WorldTile(4188, 6122, 1),
	                                                    new WorldTile(4213, 6047, 1),
	                                                    new WorldTile(4190, 6027, 1),
	                                                    new WorldTile(4161, 6027, 1),
	                                                    new WorldTile(4165, 6027, 1),
	                                                    new WorldTile(4149, 6017, 1),
	                                                    new WorldTile(4133, 6022, 1),
	                                                    new WorldTile(4187, 6122, 1),
	                                                    new WorldTile(4172, 6137, 1),
	                                                    new WorldTile(4139, 6035, 1),
	                                                    new WorldTile(4141, 6082, 1) } };

	public SiphonActionNodes(Nodes nodes, WorldObject node) {
		this.nodes = nodes;
		this.node = node;
	}

	public enum Nodes {

		CYCLONE(70455, 16596, 19, 1, 24215),

		MIND_STORM(70456, 16596, 20, 1, 24217),

		WATER_POOL(70457, 16596, 25.3, 5, 24214),

		ROCK_FRAGMENT(70458, 16596, 28.6, 9, 24216),

		FIRE_BALL(70459, 16596, 34.8, 14, 24213),

		VINE(70460, 16596, 34.8, 17, 24214, 24216),

		FLESHLY_GROWTH(70461, 16596, Utils.random(30.3, 34.3), 20, 24218),

		FIRE_STORM(70462, 16596, Utils.random(22.8, 41.7), 27, 24213, 24215),

		CHAOTIC_CLOUD(70463, 16596, 61.6, 35, 24221),

		NEBULA(70464, 16596, Utils.random(63.8, 85.6), 40, 24223, 24224),

		SHIFTER(70465, 16596, 86.8, 44, 24220),

		JUMPER(70466, 16596, 107.8, 54, 24222),

		SKULLS(70467, 16596, 120, 65, 24219),

		BLOOD_POOL(70468, 16596, 146.3, 77, 24225),

		BLOODY_SKULLS(70469, 16596, Utils.random(144, 175.5), 83, 24219, 24225),

		LIVING_SOUL(70470, 16596, 213, 90, 24226),

		UNDEAD_SOUL(70471, 16596, Utils.random(144, 255.5), 95, 24219, 24226);

		private int objectId;

		private int emoteId;

		private double xp;

		private int levelRequired;

		private int[] runeId;

		Nodes(int objectId, int emoteId, double xp, int levelRequired, int... runeId) {
			this.objectId = objectId;
			this.emoteId = emoteId;
			this.xp = xp;
			this.levelRequired = levelRequired;
			this.runeId = runeId;
		}

		public int getObjectId() {
			return objectId;
		}

		public void setObjectId(int objectId) {
			this.objectId = objectId;
		}

		public int getEmoteId() {
			return emoteId;
		}

		public void setEmoteId(int emoteId) {
			this.emoteId = emoteId;
		}

		public int[] getRuneId() {
			return runeId;
		}

		public double getXp() {
			return xp;
		}

		public void setXp(int xp) {
			this.xp = xp;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public void setLevelRequired(int levelRequired) {
			this.levelRequired = levelRequired;
		}
	}

	public static boolean siphion(Player player, WorldObject object) {
		Nodes node = getNode(object.getId());
		if (node == null) {
			return false;
		}
		player.getActionManager().setAction(new SiphonActionNodes(node, object));
		return true;
	}

	public static void init() {
		for (int j = 0; j < 3; j++) {
			if (j >= 2) {
				if (Utils.random(1) == 0) {
					continue;
				}
			}
			for (int i = 0; i < (NODE_TILES[0].length + NODE_TILES[1].length); i++) {
				Nodes node = getNode(getRandomNodeId());
				WorldTile tile = getNodeWorldTile(node);
				World.spawnObject(new WorldObject(getRandomNodeId(), 10, 0, tile.getX(), tile.getY(), tile.getPlane(), getFixedLife(node)));
			}
		}
	}

	public static WorldTile getNodeWorldTile(Nodes node) {
		WorldTile tile = NODE_TILES[Utils.random(NODE_TILES.length)][node.getLevelRequired() >= 83 ? 0 : 1];
		WorldTile teleTile = tile;
		for (int trycount = 0; trycount < 10; trycount++) {
			teleTile = new WorldTile(tile, 2);
			if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), 1)) {
				break;
			}
			teleTile = tile;
		}
		return teleTile;
	}

	private static int getRandomNodeId() {
		int index = Utils.random(Nodes.values().length);
		return Nodes.values()[index].getObjectId();
	}

	private static Nodes getNode(int id) {
		for (Nodes node : Nodes.values()) {
			if (node.objectId == id) {
				return node;
			}
		}
		return null;
	}

	@Override
	public boolean start(Player player) {
		return checkAll(player);
	}

	public boolean checkAll(final Player player) {
		if (World.getStandardObject(node) == null) {
			stop(player);
			return false;
		}
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < nodes.getLevelRequired()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a runecrafting level of " + nodes.getLevelRequired() + " to siphon from that node.");
			return false;
		}
		if (!started && (!player.withinDistance(node, 6) || !player.clipedProjectile(node, true))) {
			player.getPackets().sendGameMessage("You can't reach that!");
			return true;
		}
		if (!player.getInventory().containsItem(24227, 1)) {
			if (!player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
			} else {
				player.getPackets().sendGameMessage("You don't have any rune essence to siphon from that node.", true);
			}
			return false;
		}
		if (!started) {
			player.resetWalkSteps();
			player.setNextAnimation(new Animation(16596));
			started = true;
		}
		return true;
	}

	private static int getFixedLife(Nodes node) {
		return Utils.getRandom(node.getLevelRequired()) + 20;
	}

	private void processNodeDestroy(final Player player) {
		player.getPackets().sendGameMessage("The node you were siphoning from has been depleted of energy.", true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getPackets().sendGameMessage("You pick up the essence left by the node.", true);
				player.setNextAnimation(new Animation(16599));
				player.getInventory().addItem(24227, 50);
				World.removeObject(node);
				WorldTile tile = getNodeWorldTile(nodes);
				World.spawnObject(new WorldObject(getRandomNodeId(), node.getType(), node.getRotation(), tile.getX(), tile.getY(), tile.getPlane(), getFixedLife(nodes)));
				stop();
			}
		}, 2);
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(final Player player) {
		if (started) {
			if (node.getLife() == 0) {
				processNodeDestroy(player);
			} else {
				node.setLife(node.getLife() - 1);
				int level = player.getSkills().getLevel(Skills.RUNECRAFTING);
				if (level < 50 ? Utils.getRandom(2) == 1 : Utils.getRandom(1) == 1) {
					player.getInventory().addItem(nodes.getRuneId()[Utils.random(nodes.getRuneId().length)], 1);
					player.getInventory().deleteItem(24227, 1);
					if (player.getControllerManager().getController() != null && player.getControllerManager().getController() instanceof RunespanControler) {
						for (int runeId : nodes.getRuneId()) {
							((RunespanControler) player.getControllerManager().getController()).addRunespanPoints(runeId);
						}
					}
					double totalXp = nodes.getXp();
					if (Runecrafting.hasRcingSuit(player)) {
						totalXp *= 1.025;
					}
					player.getSkills().addXp(Skills.RUNECRAFTING, totalXp);
					player.setNextGraphics(new Graphics(3071));
				}
				player.setNextAnimation(new Animation(nodes.getEmoteId()));
				player.setNextFaceWorldTile(node);
				World.sendProjectile(node, node, player, 3060, 31, 35, 35, 0, 2, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.setNextGraphics(new Graphics(3062));
					}
				}, 1);
			}
		}
		return 1;
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(16599));
		setActionDelay(player, 3);
	}
}
