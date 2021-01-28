package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.thieving.Thieving;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class Wilderness extends Controller {

	/**
	 * The id of the wilderness token
	 */
	public static final int WILDERNESS_TOKEN = 4278;

	/**
	 * The modifier for all wilderness combat levels
	 */
	public static final int WILDERNESS_LEVEL_MODIFIER = 0;

	/**
	 * The level players are allowed to teleport at
	 */
	public static final int WILDERNESS_TELEPORT_LEVEL = 20 + WILDERNESS_LEVEL_MODIFIER;

	/**
	 * If we're showing the skull on the wilderness level component
	 */
	private boolean showingSkull;

	/**
	 * The amount of experience the player has received in the wilderness
	 */
	private double experienceReceived = 0;

	@Override
	public void start() {
		checkLentItems(player);
		checkBoosts(player);
		moved();
	}

	@Override
	public void removeController() {
		super.removeController();
		toggleSafeIcon(false);
		player.getAppearence().generateAppearenceData();
	}

	@Override
	public boolean keepCombating(Entity target) {
		if (target instanceof NPC) {
			return true;
		}
		if (!canAttack(target)) {
			return false;
		}
		if (target.getAttackedBy() != player && player.getAttackedBy() != target) {
			player.setWildernessSkull();
		}
		if (player.getCombatDefinitions().getSpellId() <= 0 && Utils.inCircle(new WorldTile(3105, 3933, 0), target, 24)) {
			player.getPackets().sendGameMessage("You can only use magic in the arena.");
			return false;
		}
		return true;
	}

	@Override
	public boolean canAttack(Entity target) {
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (player.isCanPvp() && !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("That player is not in the wilderness.");
				return false;
			}
			/*if (p2.getFacade().blacklistContains(player.getDisplayName())) {
				player.sendMessage("That player has you blacklisted.");
				return false;
			}
			if (player.getFacade().blacklistContains(p2.getDisplayName())) {
				player.sendMessage("You cannot attack a player you have blacklisted.");
				return false;
			}*/
			if (!canHit(target)) {
				player.sendMessage("You must travel deeper into the wilderness to attack that player.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void trackXP(int skillId, double addedXp) {
		if (skillId > 6) {
			experienceReceived += addedXp;
		}
	}

	@Override
	public double getExperienceModifier(int skillId, double experience) {
		if (skillId > 6) {
			double experienceModifier = getExperienceGainedModifier(experienceReceived);
			double wildernessModifier = getWildernessLevelModifier(getWildLevel());
			//			System.out.println("experienceModifier=" + experienceModifier + ", wildernessModifier=" + wildernessModifier + ", modifier=" + modifier + ", experienceReceived=" + experienceReceived);
			return 1 + (experienceModifier + wildernessModifier);
		}
		return -1;
	}

	@Override
	public boolean canHit(Entity target) {
		if (target instanceof NPC) {
			return true;
		}
		Player p2 = (Player) target;
		return Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) <= getWildLevel();
	}

	@Override
	public void process() {
		checkLentItems(player);
	}

	public static void checkLentItems(Player player) {
		boolean removedLent = false;
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			if (item.getDefinitions().isLended()) {
				player.getInventory().deleteItem(item);
				player.getBank().addItem(item.getId(), item.getAmount(), true);
				removedLent = true;
			}
		}
		for (Item item : player.getEquipment().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			if (item.getDefinitions().isLended()) {
				player.getEquipment().deleteItem(item.getId(), item.getAmount());
				player.getBank().addItem(item.getId(), item.getAmount(), true);
				removedLent = true;
			}
		}
		if (removedLent) {
			player.getAppearence().generateAppearenceData();
			player.sendMessage("Some lent items you had were sent to your bank.");
		}
	}

	@Override
	public void moved() {
		boolean isAtWild = isAtWild(player);
		boolean isAtWildSafe = isAtWildSafe(player);

		if (!showingSkull && isAtWild && !isAtWildSafe) {
			showingSkull = true;
			player.setCanPvp(true);
			showSkull();
			player.getAppearence().generateAppearenceData();
		} else if (showingSkull && (isAtWildSafe || !isAtWild)) {
			removeIcon((!isAtWild && !isAtWildSafe), (!(!isAtWild && !isAtWildSafe)));
			toggleSafeIcon(isAtWildSafe(player));
		} else if (!isAtWildSafe && !isAtWild) {
			player.setCanPvp(false);
			removeIcon(true, false);
			removeController();
		} else if (isAtWild) {
			updateWildLevel();
		}
	}

	@Override
	public void magicTeleported(int type) {
		moved();
	}

	@Override
	public void sendInterfaces() {
		if (isAtWild(player)) {
			showSkull();
		}
	}

	/**
	 * If we are in the wilderness
	 *
	 * @param tile
	 * 		The tile to check for
	 */
	public static boolean isAtWild(WorldTile tile) {
		return isAtWildSafe(tile) || getWildLevel(tile) > WILDERNESS_LEVEL_MODIFIER;
	}

	public void showSkull() {
		updateWildLevel();
		player.getPackets().sendHideIComponent(745, 6, false);
	}

	/**
	 * If the tile is at the safe area of the wilderness
	 */
	public static boolean isAtWildSafe(WorldTile tile) {
		return (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() <= 3524 && tile.getY() >= 3523);
	}

	public void updateWildLevel() {
		int lowest = player.getSkills().getCombatLevel() - getWildLevel() < 3 ? 3 : player.getSkills().getCombatLevel() - getWildLevel();
		int highest = player.getSkills().getCombatLevel() + getWildLevel() > 138 ? 138 : player.getSkills().getCombatLevel() + getWildLevel();
		if (player.getInterfaceManager().onResizable()) {
			player.getPackets().sendIComponentText(746, 17, "Level: " + getWildLevel() + "<br>" + lowest + " - " + highest);
		} else {
			player.getPackets().sendIComponentText(548, 10, "Level: " + getWildLevel());
			player.getPackets().sendIComponentText(548, 11, "" + lowest + " - " + highest);
		}
		toggleSafeIcon(isAtWildSafe(player));
	}

	private void toggleSafeIcon(boolean show) {
		player.getPackets().sendHideIComponent(745, 3, !show);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		if (getWildLevel() > WILDERNESS_TELEPORT_LEVEL) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		if (getWildLevel() > WILDERNESS_TELEPORT_LEVEL + 10) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (isDitch(object.getId())) {
			player.getLockManagement().lockAll();
			player.setNextAnimation(new Animation(6132));
			final WorldTile toTile = new WorldTile(object.getRotation() == 1 || object.getRotation() == 3 ? object.getX() + 2 : player.getX(), object.getRotation() == 0 || object.getRotation() == 2 ? object.getY() - 1 : player.getY(), object.getPlane());
			final WorldTile fromTile = new WorldTile(player);
			player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2, object.getRotation() == 0 || object.getRotation() == 2 ? ForceMovement.SOUTH : ForceMovement.WEST));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					removeIcon(true, false);
					removeController();
					player.setNextWorldTile(toTile);
					player.setNextFaceWorldTile(fromTile);
					player.resetReceivedDamage();
					player.getLockManagement().unlockAll();
				}
			}, 1);
			return false;
		} else if (object.getId() == 2557 || object.getId() == 65717) {
			player.getPackets().sendGameMessage("It seems it is locked, maybe you should try something else.");
			return false;
		}
		return true;
	}

	public static boolean isDitch(int id) {
		return id >= 1440 && id <= 1444 || id >= 65076 && id <= 65087;
	}

	/**
	 * This method handles the removing of the on screen wilderness interfaces.
	 *
	 * @param force
	 * 		If we should force the removal of the interfaces and the text.
	 * @param skullOnly
	 * 		If we should only remove the skull on the screen, and leave the level text.
	 */
	public void removeIcon(boolean force, boolean skullOnly) {
		if (force || showingSkull) {
			showingSkull = false;
			player.setCanPvp(false);
			if (!skullOnly) {
				player.getPackets().sendIComponentText(player.getInterfaceManager().onResizable() ? 746 : 548, player.getInterfaceManager().onResizable() ? 17 : 10, "");
				player.getPackets().sendIComponentText(player.getInterfaceManager().onResizable() ? 746 : 548, player.getInterfaceManager().onResizable() ? 17 : 11, "");
			}
			player.getPackets().sendHideIComponent(745, 6, true);
			player.getAppearence().generateAppearenceData();
			player.getEquipment().refresh();
		}
	}

	@Override
	public boolean processObjectClick2(final WorldObject object) {
		if (object.getId() == 2557 || object.getId() == 65717) {
			Thieving.pickDoor(player, object);
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.removeDamage(player);
						killer.increaseKillCount(player);
					}
					player.sendItemsOnDeath(killer);
					player.getEquipment().init();
					player.getInventory().init();
					player.reset();
					player.setNextWorldTile(new WorldTile(GameConstants.RESPAWN_PLAYER_LOCATION));
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					removeIcon(true, false);
					removeController();
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean login() {
		moved();
		return false;
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void forceClose() {
		removeIcon(true, false);
	}

	/**
	 * Gets the modifier result based on the amount of experience gained in the wilderness
	 *
	 * @param experienceGained
	 * 		The amount of experience gained
	 */
	public static double getExperienceGainedModifier(double experienceGained) {
		int division = (int) experienceGained / 20000;
		double start = 0.0D;
		for (int i = 0; i < division; i++) {
			start += 0.01;
		}
		if (start >= 0.15) {
			start = 0.15;
		}
		return start;
	}

	/**
	 * The modifier for incoming experienced, based on how deep you are in the wilderness
	 *
	 * @param wildernessLevel
	 * 		The wilderness level
	 */
	public static double getWildernessLevelModifier(int wildernessLevel) {
		return wildernessLevel / (double) 300;
	}

	public int getWildLevel() {
		return getWildLevel(player);
	}

	public static int getWildLevel(WorldTile tile) {
		int x = tile.getX(), y = tile.getY();
		int level = 0;
		if (y >= 10302 && y <= 10357) {
			level = (byte) ((y - 9912) / 8 + 1);
		}
		if (x > 2935 && x < 3400 && y > 3524 && y < 4000) {
			level = (byte) ((Math.ceil((y) - 3520D) / 8D) + 1);
		}
		if (y > 10050 && y < 10179 && x > 3008 && x < 3144) {
			level = (byte) ((Math.ceil((y) - 10048D) / 8D) + 17);
		}
		return level + WILDERNESS_LEVEL_MODIFIER;
	}

	public static void checkBoosts(Player player) {
		boolean changed = false;
		int level = player.getSkills().getLevelForXp(Skills.ATTACK);
		int maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.ATTACK)) {
			player.getSkills().setLevel(Skills.ATTACK, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.STRENGTH);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.STRENGTH)) {
			player.getSkills().setLevel(Skills.STRENGTH, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.DEFENCE);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.DEFENCE)) {
			player.getSkills().setLevel(Skills.DEFENCE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.RANGE);
		maxLevel = (int) (level + 5 + (level * 0.1));
		if (maxLevel < player.getSkills().getLevel(Skills.RANGE)) {
			player.getSkills().setLevel(Skills.RANGE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.MAGIC);
		maxLevel = level + 5;
		if (maxLevel < player.getSkills().getLevel(Skills.MAGIC)) {
			player.getSkills().setLevel(Skills.MAGIC, maxLevel);
			changed = true;
		}
		if (changed) {
			player.getPackets().sendGameMessage("Your extreme potion bonus has been reduced.");
		}
	}

	/**
	 * Finds how many points the player should get for ending a killstreak
	 *
	 * @param killstreakEnded
	 * 		The killstreak ended size
	 */
	public static int getPointRewardFromKillstreakEnding(int killstreakEnded) {
		if (killstreakEnded <= 3) {
			return 0;
		} else {
			int baseCount = 30;
			int ksAdditive = killstreakEnded * 5;
			return (int) (baseCount + (ksAdditive * 1.75));
		}
	}

	/**
	 * Finds the amount of wilderness points the player should receive for their killstreak
	 *
	 * @param killstreak
	 * 		The killstreak the player is on
	 */
	public static int wildernessPointsAfterKillstreakModifier(int killstreak) {
		int baseModifier = 100;
		int ksModifier = (int) (killstreak * 2.85);
		int reward = baseModifier + ksModifier;
		if (reward >= 200) {
			reward = 200;
		}
		return reward;
	}

}
