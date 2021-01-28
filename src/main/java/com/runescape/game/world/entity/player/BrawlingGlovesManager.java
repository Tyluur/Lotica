package com.runescape.game.world.entity.player;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.utility.ChatColors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Jul 21, 2013
 */
public final class BrawlingGlovesManager implements Serializable {

	private static final long serialVersionUID = -5712578289476610260L;

	private Map<BrawlingGloveType, Long> charges = new HashMap<BrawlingGloveType, Long>();

	/**
	 * The player
	 */
	private transient Player player;

	public BrawlingGlovesManager() {

	}

	/**
	 * Gets the experience modifier for the skill. If we have an existing brawling gloves session we will have increased
	 * modifiers
	 *
	 * @param skill
	 * 		The skill
	 */
	public double getExperienceModifier(int skill) {
		BrawlingGloveType type = getBrawlingType();
		if (type == null) {
			return 1;
		}
		if (type != BrawlingGloveType.MELEE && !correctSkill(type, skill)) {
			return 1;
		} else if (type == BrawlingGloveType.MELEE && !isCombatSkill(skill)) {
			return 1;
		}
		if (!charges.containsKey(type)) {
			charges.put(type, System.currentTimeMillis());
			player.sendMessage("<col=" + ChatColors.ORANGE + ">This is your first time using brawling gloves (" + type.name().toLowerCase() + ").");
		}
		long startTime = charges.get(type);
		long timeUsed = System.currentTimeMillis() - startTime;
		if (timeUsed >= TimeUnit.MINUTES.toMillis(30)) {
			deleteCharge(type);
			return 1;
		}
		if (TimeUnit.MILLISECONDS.toMinutes(timeUsed) % 5 == 0 && (player.getAttributes().get("brawling_time") == null || (long) player.getAttributes().get("brawling_time") != TimeUnit.MILLISECONDS.toMinutes(timeUsed))) {
			long time = (TimeUnit.MINUTES.toMillis(30) + (charges.get(type))) - System.currentTimeMillis();
			player.sendMessage("<col=" + ChatColors.BLUE + ">You have " + TimeUnit.MILLISECONDS.toMinutes(time) + " minutes left with brawling gloves (" + type.name().toLowerCase() + ").");
			player.getAttributes().put("brawling_time", TimeUnit.MILLISECONDS.toMinutes(timeUsed));
		}
		double amount = 1.5;
		switch (type) {
			case AGILITY:
			case COOKING:
			case FIREMAKING:
			case FISHING:
			case HUNTER:
			case MAGIC:
			case MINING:
			case PRAYER:
			case RANGED:
			case SMITHING:
			case THIEVING:
			case WOODCUTTING:
				return (Wilderness.isAtWild(player) ? (amount * 2) : amount);
			case MELEE:
				return (Wilderness.isAtWild(player) ? (amount * 2) : amount);
		}
		return 1;
	}

	/**
	 * Identifies the type of brawling gloves we have equipped
	 *
	 * @return A {@code Types} {@code Object}
	 */
	public BrawlingGloveType getBrawlingType() {
		int gloves = player.getEquipment().getGlovesId();
		if (gloves != -1) {
			String name = ItemDefinitions.getItemDefinitions(gloves).getName();
			if (name.startsWith("Brawling gloves")) {
				String skillName = name.split("\\(")[1].split("\\)")[0];
				if (skillName.equalsIgnoreCase("wc")) {
					skillName = "Woodcutting";
				} else if (skillName.equalsIgnoreCase("fm")) {
					skillName = "Firemaking";
				}
				for (BrawlingGloveType type : BrawlingGloveType.values()) {
					if (type.name().equalsIgnoreCase(skillName)) {
						return type;
					}
				}
			}
		}
		return null;
	}

	private boolean correctSkill(BrawlingGloveType type, int skill) {
		switch (type) {
			case MELEE:
			case RANGED:
			case MAGIC:
				if (skill == 3) {
					return true;
				}
				break;
			default:
				break;
		}
		String name = Skills.SKILL_NAME[skill];
		return name.equalsIgnoreCase(type.name());
	}

	/**
	 * If the skill is a combat skill
	 *
	 * @param skill
	 * 		The skill
	 */
	private boolean isCombatSkill(int skill) {
		return skill == Skills.ATTACK || skill == Skills.STRENGTH || skill == Skills.DEFENCE || skill == Skills.HITPOINTS;
	}

	/**
	 * Removes the brawling glove charges
	 *
	 * @param type
	 * 		The type of charge
	 */
	private void deleteCharge(BrawlingGloveType type) {
		Iterator<Entry<BrawlingGloveType, Long>> it$ = charges.entrySet().iterator();
		while (it$.hasNext()) {
			Entry<BrawlingGloveType, Long> entry = it$.next();
			if (entry.getKey() == type) {
				it$.remove();
			}
		}
		player.getEquipment().deleteItem(player.getEquipment().getGlovesId(), player.getEquipment().getItems().getNumberOf(player.getEquipment().getGlovesId()));
		player.getAppearence().getAppeareanceData();
		player.sendMessage("<col=" + ChatColors.RED + ">Your 30 minutes with brawling gloves (" + type.name().toLowerCase() + ") is over.");
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private enum BrawlingGloveType {
		MELEE,
		MAGIC,
		RANGED,
		AGILITY,
		COOKING,
		FIREMAKING,
		FISHING,
		HUNTER,
		MINING,
		PRAYER,
		SMITHING,
		THIEVING,
		WOODCUTTING
	}

}