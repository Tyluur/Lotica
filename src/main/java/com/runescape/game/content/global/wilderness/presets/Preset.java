package com.runescape.game.content.global.wilderness.presets;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/27/2015
 */
public class Preset {

	/** The skills in this preset */
	private List<Double> skills;

	/** The list of inventory items on this preset */
	private List<Item> inventoryItems;

	/** The list of items equipped on this preset */
	private List<Item> equippedItems;

	/** The preset of the spellbook */
	private Spellbook spellbook;

	/** If this preset uses ancient curses */
	private boolean onCurses;

	/**
	 * Converts the player's data into a preset
	 *
	 * @param player
	 * 		The player
	 */
	public Preset convertPreset(Player player) {
		skills = convertSkillsToList(player);
		inventoryItems = convertInventoryToList(player);
		equippedItems = convertEquipmentToList(player);
		spellbook = Spellbook.getSpellbookInstance(player);
		onCurses = player.getPrayer().isAncientCurses();
		return this;
	}

	/**
	 * Converts the player's current levels to a list
	 *
	 * @param player
	 * 		The player
	 */
	private List<Double> convertSkillsToList(Player player) {
		List<Double> skills = new ArrayList<>();
		for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
			skills.add(player.getSkills().getXp(i));
		}
		return skills;
	}

	/**
	 * This method converts the player's inventory to a list object
	 *
	 * @param player
	 * 		The player
	 */
	private List<Item> convertInventoryToList(Player player) {
		return new ArrayList<>(Arrays.asList(player.getInventory().getItems().toArray()));
	}

	/**
	 * This method converts the player's equipment to a list object
	 *
	 * @param player
	 * 		The player
	 */
	private List<Item> convertEquipmentToList(Player player) {
		return new ArrayList<>(Arrays.asList(player.getEquipment().getItems().toArray()));
	}

	/**
	 * Gets the combat level of the skills in
	 */
	public int getCombatLevel() {
		double attack = Skills.getLevelByExperience(skills.get(0), Skills.ATTACK);
		double defence = Skills.getLevelByExperience(skills.get(1), Skills.DEFENCE);
		double strength = Skills.getLevelByExperience(skills.get(2), Skills.STRENGTH);
		double hp = Skills.getLevelByExperience(skills.get(3), Skills.HITPOINTS);
		double prayer = Skills.getLevelByExperience(skills.get(5), Skills.PRAYER);
		double ranged = Skills.getLevelByExperience(skills.get(4), Skills.RANGE);
		double magic = Skills.getLevelByExperience(skills.get(6), Skills.MAGIC);
		int combatLevel = (int) ((defence + hp + Math.floor(prayer / 2)) * 0.25) + 1;
		double melee = (attack + strength) * 0.325;
		double ranger = Math.floor(ranged * 1.5) * 0.325;
		double mage = Math.floor(magic * 1.5) * 0.325;
		if (melee >= ranger && melee >= mage) {
			combatLevel += melee;
		} else if (ranger >= melee && ranger >= mage) {
			combatLevel += ranger;
		} else if (mage >= melee && mage >= ranger) {
			combatLevel += mage;
		}
		return combatLevel;
	}

	public List<Double> getSkills() {
		return skills;
	}

	public List<Item> getInventoryItems() {
		return inventoryItems;
	}

	public List<Item> getEquippedItems() {
		return equippedItems;
	}

	public boolean isOnCurses() {
		return onCurses;
	}

	public Spellbook getSpellbook() {
		return spellbook;
	}

	public enum Spellbook {
		MODERN(192),
		ANCIENT(193),
		LUNAR(430);

		Spellbook(int spellbookId) {
			this.spellbookId = spellbookId;
		}

		/**
		 * The spellbook id
		 */
		private final int spellbookId;

		/**
		 * Gets the spellbook instance based on the player's current spellbook
		 *
		 * @param player
		 * 		The player
		 */
		public static Spellbook getSpellbookInstance(Player player) {
			for (Spellbook spellbook : Spellbook.values()) {
				if (spellbook.spellbookId == player.getCombatDefinitions().getSpellBook()) {
					return spellbook;
				}
			}
			return null;
		}
	}

}
