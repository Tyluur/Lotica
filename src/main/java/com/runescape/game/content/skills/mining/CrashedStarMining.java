package com.runescape.game.content.skills.mining;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.workers.tasks.impl.InformationTabTick;
import com.runescape.workers.tasks.impl.ShootingStarTick;
import com.runescape.workers.tasks.impl.ShootingStarTick.ShootingStar;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 26, 2015
 */
public class CrashedStarMining extends MiningBase {

	/**
	 * The stardust item id
	 */
	private static final int STARDUST = 13727;

	private PickAxeDefinitions axeDefinitions;

	public CrashedStarMining(ShootingStar star) {
		this.star = star;
	}

	@Override
	public boolean start(Player player) {
		axeDefinitions = getPickAxeDefinitions(player);
		player.removeAttribute("stardust_storage_full");
		if (!checkAll(player)) {
			return false;
		}
		player.getPackets().sendGameMessage("You swing your pickaxe at the rock.", true);
		setActionDelay(player, getMiningDelay(player));
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (!checkAll(player)) {
			return false;
		}
		player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
		return checkRock(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.getSkills().addXpNoModifier(Skills.MINING, star.getStage().getExp() * 10);
		if (canStoreStardust(player)) {
			player.getInventory().addItem(STARDUST, 1);
		} else {
			if (!player.getAttribute("stardust_storage_full", false)) {
				player.putAttribute("stardust_storage_full", true);
				player.sendMessage("You can't hold any more stardust in your inventory.");
			}
		}
		star.deductHealth(1);
		if (star.getHealth() <= 0) {
			star.updateStage();
		}
		return getMiningDelay(player);
	}

	private int getMiningDelay(Player player) {
		int baseDelay = star.getStage().getDelay();
		int levelModif = Math.floorDiv(player.getSkills().getLevelForXp(Skills.MINING), 10);
		int levelDelay = 10 - levelModif;
		int pickDelay = axeDefinitions.getPickAxeTime() / 5;
		int totalDelay = (baseDelay + levelDelay) - pickDelay;
		return totalDelay;
	}

	private boolean checkRock(Player player) {
		return World.containsObjectWithId(star.getId(), star);
	}

	private boolean checkAll(Player player) {
		if (axeDefinitions == null) {
			player.getPackets().sendGameMessage("You do not have a pickaxe or do not have the required level to use the pickaxe.");
			return false;
		}
		if (!hasMiningLevel(player)) { return false; }
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		ShootingStarTick instance = InformationTabTick.shootingStarTick;
		if (instance != null && !instance.starExists()) {
			return false;
		}
		return true;
	}

	private boolean hasMiningLevel(Player player) {
		if (player.getSkills().getLevel(Skills.MINING) < star.getStage().getLevelRequired()) {
			player.getPackets().sendGameMessage("You need a mining level of " + star.getStage().getLevelRequired() + " to mine this star.");
			return false;
		}
		return true;
	}

	private boolean canStoreStardust(Player player) {
		return player.getInventory().getAmountOf(STARDUST) < 200;
	}

	/**
	 * @return the star
	 */
	public ShootingStar getStar() {
		return star;
	}

	/**
	 * The shooting star
	 */
	private final ShootingStar star;

}
