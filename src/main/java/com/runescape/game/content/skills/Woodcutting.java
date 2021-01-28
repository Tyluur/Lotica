package com.runescape.game.content.skills;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.global.minigames.duel.DuelArena;
import com.runescape.game.content.global.minigames.duel.DuelControler;
import com.runescape.game.content.skills.firemaking.Firemaking.Fire;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Chop_Chop;
import com.runescape.game.world.entity.player.achievements.hard.Magic_Tree_Hater;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

public final class Woodcutting extends Action {

	private static final int[] BIRD_NESTS = { 5070, 5071, 5072, 5073, 5074, 7413, 11966 };

	private WorldObject tree;

	private TreeDefinitions definitions;

	private HatchetDefinitions hatchet;

	private boolean usingBeaver = false;

	private boolean usedDeplateAurora;

	@Override
	public boolean start(Player player) {
		if (!checkAll(player)) { return false; }
		player.getPackets().sendGameMessage(usingBeaver ? "Your beaver uses its strong teeth to chop down the tree..." : "You swing your hatchet at the " + (TreeDefinitions.IVY == definitions ? "ivy" : "tree") + "...", true);
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	}

	private boolean checkAll(Player player) {
		hatchet = getHatchet(player);
		if (hatchet == null) {
			player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
			return false;
		}
		if (!hasWoodcuttingLevel(player)) { return false; }
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		return true;
	}

	private int getWoodcuttingDelay(Player player) {
		int summoningBonus = player.getFamiliar() != null ? (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 10 : 0 : 0;
		int wcTimer = definitions.getLogBaseTime() - (player.getSkills().getLevel(8) + summoningBonus) - Utils.getRandom(hatchet.axeTime);
		if (wcTimer < 1 + definitions.getLogRandomTime()) {
			wcTimer = 1 + Utils.getRandom(definitions.getLogRandomTime());
		}
		wcTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
		return wcTimer;
	}

	public static HatchetDefinitions getHatchet(Player player) {
		HatchetDefinitions hatchet = null;
		for (HatchetDefinitions def : HatchetDefinitions.values()) {
			if (hatchet == HatchetDefinitions.INFERNO) {
				if (player.getSkills().getLevel(Skills.FIREMAKING) < 92) { continue; }
			}
			if ((player.getInventory().contains(def.itemId) || player.getEquipment().getWeaponId() == def.itemId) && player.getSkills().getLevelForXp(Skills.WOODCUTTING) >= def.getLevelRequried()) {
				hatchet = def;
			}
		}
		return hatchet;
	}

	private boolean hasWoodcuttingLevel(Player player) {
		if (definitions.getLevel() > player.getSkills().getLevel(8)) {
			player.getPackets().sendGameMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (usingBeaver) {
			player.getFamiliar().setNextAnimation(new Animation(7722));
			player.getFamiliar().setNextGraphics(new Graphics(1458));
		} else { player.setNextAnimation(new Animation(hatchet.emoteId)); }
		return checkTree(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addLog(definitions, hatchet, player);
		if (definitions == TreeDefinitions.WILLOW && player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
			player.getInventory().addItemDrop(Wilderness.WILDERNESS_TOKEN, 2);
		}
		if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
			usedDeplateAurora = true;
		} else if (Utils.getRandom(definitions.getRandomLifeProbability()) == 0) {
			long time = definitions.respawnDelay * 600;
			if (player.getRegionId() != 12084) {
				World.spawnTemporaryObject(new WorldObject(definitions.getStumpId(), getTree().getType(), getTree().getRotation(), getTree().getX(), getTree().getY(), getTree().getPlane()), time);
				if (!tree.isSpawned()) {
					CoresManager.schedule(() -> {
						try {
							World.spawnObject(tree);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}, time + 1000, TimeUnit.MILLISECONDS);
				}
			}
			if (getTree().getPlane() < 3 && definitions != TreeDefinitions.IVY) {
				WorldObject object = World.getStandardObject(new WorldTile(getTree().getX() - 1, getTree().getY() - 1, getTree().getPlane() + 1));

				if (object == null) {
					object = World.getStandardObject(new WorldTile(getTree().getX(), getTree().getY() - 1, getTree().getPlane() + 1));
					if (object == null) {
						object = World.getStandardObject(new WorldTile(getTree().getX() - 1, getTree().getY(), getTree().getPlane() + 1));
						if (object == null) {
							object = World.getStandardObject(new WorldTile(getTree().getX(), getTree().getY(), getTree().getPlane() + 1));
						}
					}
				}
				if (object != null) {
					World.removeTemporaryObject(object, time);
				}
			}
			if (definitions == TreeDefinitions.MAGIC) {
				AchievementHandler.incrementProgress(player, Magic_Tree_Hater.class, 1);
			}
			player.setNextAnimation(new Animation(-1));
			return -1;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return -1;
		}
		return getWoodcuttingDelay(player);
	}

	public static void addLog(TreeDefinitions definitions, HatchetDefinitions hatchet, Player player) {
		String logName = ItemDefinitions.forId(definitions.getLogsId()).getName().toLowerCase();
		double xpBoost = 1.00;
		if (player.getEquipment().getChestId() == 10939) { xpBoost += 0.008; }
		if (player.getEquipment().getLegsId() == 10940) { xpBoost += 0.006; }
		if (player.getEquipment().getHatId() == 10941) { xpBoost += 0.004; }
		if (player.getEquipment().getBootsId() == 10933) { xpBoost += 0.002; }
		if (player.getEquipment().getChestId() == 10939 && player.getEquipment().getLegsId() == 10940 && player.getEquipment().getHatId() == 10941 && player.getEquipment().getBootsId() == 10933) {
			xpBoost += 0.005;
		}
		player.getSkills().addXp(8, definitions.getXp() * xpBoost);
		boolean adzed = false;
		if (hatchet != null && hatchet == HatchetDefinitions.INFERNO && Utils.random(10) >= 8) {
			Fire fire = Fire.getFireInstance(definitions.getLogsId());
			if (fire != null) {
				boolean cantCreateFire = !World.canMoveNPC(player.getPlane(), player.getX(), player.getY(), 1) || World.getRegion(player.getRegionId()).getSpawnedObject(player) != null || player.getControllerManager().getController() instanceof DuelArena || player.getControllerManager().getController() instanceof DuelControler;
				if (!cantCreateFire) {
					World.spawnTempGroundObject(new WorldObject(fire.getFireId(), 10, 0, player.getX(), player.getY(), player.getPlane()), 592, fire.getLife());
				}
				player.getPackets().sendGameMessage("You chop some " + logName + ". The heat of the inferno adze incinerates them.", true);
				adzed = true;
			}
		} else {
			player.getInventory().addItem(definitions.getLogsId(), 1);
		}
		if (Utils.random(50) == 0) {
			player.getInventory().addItemDrop(BIRD_NESTS[Utils.random(BIRD_NESTS.length)], 1);
			player.getPackets().sendGameMessage("A bird's nest falls out of the tree!");
		}
		if (definitions == TreeDefinitions.IVY) {
			player.getPackets().sendGameMessage("You succesfully cut an ivy vine.", true);
		} else if (!adzed) {
			player.getPackets().sendGameMessage("You chop some " + logName + ".", true);
		}
		if (definitions == TreeDefinitions.NORMAL) {
			AchievementHandler.incrementProgress(player, Chop_Chop.class, 1);
		}
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

	private boolean checkTree(Player player) {
		return World.containsObjectWithId(getTree().getId(), getTree());
	}

	public WorldObject getTree() {
		return tree;
	}

	public Woodcutting(WorldObject tree, TreeDefinitions definitions) {
		this.tree = tree;
		this.definitions = definitions;
	}

	public enum HatchetDefinitions {

		BRONZE(1351, 1, 1, 879),

		IRON(1349, 1, 2, 877),

		STEEL(1353, 5, 3, 875),

		BLACK(1361, 11, 4, 873),

		MITHRIL(1355, 21, 5, 871),

		ADAMANT(1357, 31, 7, 869),

		RUNE(1359, 41, 10, 867),

		DRAGON(6739, 61, 13, 2846),

		INFERNO(13661, 61, 13, 10251);

		private int itemId, levelRequried, axeTime, emoteId;

		HatchetDefinitions(int itemId, int levelRequried, int axeTime, int emoteId) {
			this.itemId = itemId;
			this.levelRequried = levelRequried;
			this.axeTime = axeTime;
			this.emoteId = emoteId;
		}

		public int getItemId() {
			return itemId;
		}

		public int getLevelRequried() {
			return levelRequried;
		}

		public int getAxeTime() {
			return axeTime;
		}

		public int getEmoteId() {
			return emoteId;
		}
	}

	public enum TreeDefinitions {

		NORMAL(1, 25, 1511, 20, 4, 1341, 8, 0),

		EVERGREEN(1, 25, 1511, 20, 4, 57931, 8, 0),

		DEAD(1, 25, 1511, 20, 4, 12733, 8, 0),

		OAK(15, 37.5, 1521, 30, 4, 1341, 15, 15),

		WILLOW(30, 67.5, 1519, 60, 4, 1341, 51, 15),

		TEAK(30, 85, 6333, 60, 4, 1341, 51, 15),

		MAPLE(45, 100, 1517, 65, 12, 31057, 72, 10),

		MAHOGANY(50, 125, 6332, 83, 16, 1341, 51, 15),

		YEW(60, 175, 1515, 85, 13, 1341, 94, 10),

		FRUIT_TREES(1, 25, -1, 20, 4, 1341, 8, 0),

		IVY(68, 332.5, -1, 70, 17, 46319, 58, 10),

		MAGIC(75, 250, 1513, 110, 14, 37824, 121, 10),

		CURSED_MAGIC(82, 250, 1513, 80, 21, 37822, 121, 10);

		private int level;

		private double xp;

		private int logsId;

		private int logBaseTime;

		private int logRandomTime;

		private int stumpId;

		private int respawnDelay;

		private int randomLifeProbability;

		TreeDefinitions(int level, double xp, int logsId, int logBaseTime, int logRandomTime, int stumpId, int respawnDelay, int randomLifeProbability) {
			this.level = level;
			this.xp = xp;
			this.logsId = logsId;
			this.logBaseTime = logBaseTime;
			this.logRandomTime = logRandomTime;
			this.stumpId = stumpId;
			this.respawnDelay = respawnDelay;
			this.randomLifeProbability = randomLifeProbability;
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}

		public int getLogsId() {
			return logsId;
		}

		public int getLogBaseTime() {
			return logBaseTime;
		}

		public int getLogRandomTime() {
			return logRandomTime;
		}

		public int getStumpId() {
			return stumpId;
		}

		public int getRespawnDelay() {
			return respawnDelay;
		}

		public int getRandomLifeProbability() {
			return randomLifeProbability;
		}
	}

	public static class Nest {

		public static final int[][] SEEDS = { { 5312, 5283, 5284, 5313, 5285, 5286 }, { 5314, 5288, 5287, 5315, 5289 }, { 5316, 5290 }, { 5317 } };

		private static final int[] RINGS = { 1635, 1637, 1639, 1641, 1643 };

		public static boolean isNest(int id) {
			return id == 5070 || id == 5071 || id == 5072 || id == 5073 || id == 5074 || id == 7413 || id == 11966;
		}

		public static void searchNest(final Player player, final int slot) {
			player.getPackets().sendGameMessage("You search the nest...and find something in it!");
			player.getLockManagement().lockAll(1000);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					Item item = player.getInventory().getItem(slot);
					if (player != null && item != null) {
						player.getInventory().addItemDrop(getRewardForId(item.getId()), 1);
						player.getInventory().replaceItem(5075, 1, slot);
					}
				}
			});
		}

		private static int getRewardForId(int id) {
			if (id == 5070) { return 5076; } else if (id == 11966) { return 11964; } else if (id == 5071) {
				return 5078;
			} else if (id == 5072) {
				return 5077;
			} else if (id == 5074) {
				return RINGS[Utils.random(RINGS.length)];
			} else if (id == 7413 || id == 5073) {
				double random = Utils.random(0, 100);
				final int reward = random <= 39.69 ? 0 : random <= 56.41 ? 1 : random <= 76.95 ? 2 : random <= 96.4 ? 3 : 1;
				return SEEDS[reward][Utils.random(SEEDS[reward].length)];
			}
			return -1;
		}
	}
}
