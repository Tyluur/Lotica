package com.runescape.game.world.entity.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.DXPAlgorithms;
import com.runescape.game.event.interaction.button.SkillSelectionInteractionEvent;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.impl.skills.LevelUp;
import com.runescape.game.world.World;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.DailyEvents;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public final class Skills implements Serializable {

	public static final double MAXIMUM_EXP = 200000000;

	public static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2, HITPOINTS = 3, RANGE = 4, PRAYER = 5, MAGIC = 6, COOKING = 7, WOODCUTTING = 8, FLETCHING = 9, FISHING = 10, FIREMAKING = 11, CRAFTING = 12, SMITHING = 13, MINING = 14, HERBLORE = 15, AGILITY = 16, THIEVING = 17, SLAYER = 18, FARMING = 19, RUNECRAFTING = 20, CONSTRUCTION = 22, HUNTER = 21, SUMMONING = 23, DUNGEONEERING = 24;

	public static final String[] SKILL_NAME = { "Attack", "Defence", "Strength", "Constitution", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction", "Summoning", "Dungeoneering" };

	public static final byte[] XP_COUNTER_STAT_ORDER = { ATTACK, STRENGTH, DEFENCE, RANGE, MAGIC, HITPOINTS, PRAYER, AGILITY, HERBLORE, THIEVING, CRAFTING, FLETCHING, MINING, SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, RUNECRAFTING, SLAYER, FARMING, CONSTRUCTION, HUNTER, SUMMONING, DUNGEONEERING };

	private static final long serialVersionUID = -7086829989489745985L;

	private short level[];

	private double xp[];

	private double xpCounter;

	private boolean[] enabledSkillsTargets;

	private boolean[] skillsTargetsUsingLevelMode;

	private int[] skillsTargetsValues;

	private int combatRate = -1;

	private transient Player player;

	private transient double bonusExperienceGained;

	public Skills() {
		level = new short[25];
		xp = new double[25];
		for (int i = 0; i < level.length; i++) {
			level[i] = 1;
			xp[i] = 0;
		}
		level[3] = 10;
		xp[3] = 1184;
		level[HERBLORE] = 3;
		xp[HERBLORE] = 250;
		enabledSkillsTargets = new boolean[25];
		skillsTargetsUsingLevelMode = new boolean[25];
		skillsTargetsValues = new int[25];
	}

	public static int getXPForLevel(int level) {
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= level; lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			if (lvl >= level) {
				return output;
			}
			output = (int) Math.floor(points / 4);
		}
		return 0;
	}

	public static int getLevelByExperience(double exp, int skill) {
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= (skill == DUNGEONEERING ? 120 : 99); lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if ((output - 1) >= exp) {
				return lvl;
			}
		}
		return skill == DUNGEONEERING ? 120 : 99;
	}

	public void passLevels(Player p) {
		this.level = p.getSkills().level;
		this.xp = p.getSkills().xp;
	}

	public int getTargetIdByComponentId(int componentId) {
		switch (componentId) {
			case 200: // Attack
				return 0;
			case 11: // Strength
				return 1;
			case 52: // Range
				return 2;
			case 93: // Magic
				return 3;
			case 28: // Defence
				return 4;
			case 193: // Constitution
				return 5;
			case 76: // Prayer
				return 6;
			case 19: // Agility
				return 7;
			case 36: // Herblore
				return 8;
			case 60: // Theiving
				return 9;
			case 84: // Crafting
				return 10;
			case 110: // Runecrafting
				return 11;
			case 186: // Mining
				return 12;
			case 179: // Smithing
				return 13;
			case 44: // Fishing
				return 14;
			case 68: // Cooking
				return 15;
			case 172: // Firemaking
				return 16;
			case 165: // Woodcutting
				return 17;
			case 101: // Fletching
				return 18;
			case 118: // Slayer
				return 19;
			case 126: // Farming
				return 20;
			case 134: // Construction
				return 21;
			case 142: // Hunter
				return 22;
			case 150: // Summoning
				return 23;
			case 158: // Dungeoneering
				return 24;
			default:
				return -1;
		}
	}

	public int getSkillIdByTargetId(int targetId) {
		switch (targetId) {
			case 0: // Attack
				return ATTACK;
			case 1: // Strength
				return STRENGTH;
			case 2: // Range
				return RANGE;
			case 3: // Magic
				return MAGIC;
			case 4: // Defence
				return DEFENCE;
			case 5: // Constitution
				return HITPOINTS;
			case 6: // Prayer
				return PRAYER;
			case 7: // Agility
				return AGILITY;
			case 8: // Herblore
				return HERBLORE;
			case 9: // Thieving
				return THIEVING;
			case 10: // Crafting
				return CRAFTING;
			case 11: // Runecrafting
				return RUNECRAFTING;
			case 12: // Mining
				return MINING;
			case 13: // Smithing
				return SMITHING;
			case 14: // Fishing
				return FISHING;
			case 15: // Cooking
				return COOKING;
			case 16: // Firemaking
				return FIREMAKING;
			case 17: // Woodcutting
				return WOODCUTTING;
			case 18: // Fletching
				return FLETCHING;
			case 19: // Slayer
				return SLAYER;
			case 20: // Farming
				return FARMING;
			case 21: // Construction
				return CONSTRUCTION;
			case 22: // Hunter
				return HUNTER;
			case 23: // Summoning
				return SUMMONING;
			case 24: // Dungeoneering
				return DUNGEONEERING;
			default:
				return -1;
		}
	}

	public void setSkillTarget(boolean usingLevel, int skillId, int target) {
		setSkillTargetEnabled(skillId, true);
		setSkillTargetUsingLevelMode(skillId, usingLevel);
		setSkillTargetValue(skillId, target);
	}

	public void setSkillTargetEnabled(int id, boolean enabled) {
		enabledSkillsTargets[id] = enabled;
		refreshEnabledSkillsTargets();
	}

	public void setSkillTargetUsingLevelMode(int id, boolean using) {
		skillsTargetsUsingLevelMode[id] = using;
		refreshUsingLevelTargets();
	}

	public void setSkillTargetValue(int skillId, int value) {
		skillsTargetsValues[skillId] = value;
		refreshSkillsTargetsValues();
	}

	public void refreshEnabledSkillsTargets() {
		int value = Utils.get32BitValue(enabledSkillsTargets, true);
		player.getPackets().sendConfig(1966, value);
	}

	public void refreshUsingLevelTargets() {
		int value = Utils.get32BitValue(skillsTargetsUsingLevelMode, true);
		player.getPackets().sendConfig(1968, value);
	}

	public void refreshSkillsTargetsValues() {
		for (int i = 0; i < 25; i++) {
			player.getPackets().sendConfig(1969 + i, skillsTargetsValues[i]);
		}
	}

	public void restoreSkills() {
		for (int skill = 0; skill < level.length; skill++) {
			restoreSkill(skill);
			refresh(skill);
		}
	}

	public void restoreSkill(int skill) {
		if (skill == HITPOINTS) {
			player.heal(getLevelForXp(skill) * 10);
		} else if (skill == PRAYER) {
			player.getPrayer().restorePrayer(getLevelForXp(skill) * 10);
		} else {
			level[skill] = (short) getLevelForXp(skill);
		}
	}

	public void refresh(int skill) {
		player.getPackets().sendSkillLevel(skill);
	}

	public int getLevelForXp(int skill) {
		double exp = xp[skill];
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= (skill == DUNGEONEERING ? 120 : 99); lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if ((output - 1) >= exp) {
				return lvl;
			}
		}
		return skill == DUNGEONEERING ? 120 : 99;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public double[] getXp() {
		return xp;
	}

	public boolean hasRequirements(int... skills) {
		for (int i = 0; i < skills.length; i += 2) {
			int skillId = skills[i];
			if (skillId == CONSTRUCTION || skillId == FARMING) {
				continue;
			}
			int skillLevel = skills[i + 1];
			if (getLevelForXp(skillId) < skillLevel) {
				return false;
			}

		}
		return true;
	}

	public int drainLevel(int skill, int drain) {
		int drainLeft = drain - level[skill];
		if (drainLeft < 0) {
			drainLeft = 0;
		}
		level[skill] -= drain;
		if (level[skill] < 0) {
			level[skill] = 0;
		}
		refresh(skill);
		return drainLeft;
	}

	public int getCombatLevelWithSummoning() {
		return getCombatLevel() + getSummoningCombatLevel();
	}

	public int getCombatLevel() {
		int attack = getLevelForXp(0);
		int defence = getLevelForXp(1);
		int strength = getLevelForXp(2);
		int hp = getLevelForXp(3);
		int prayer = getLevelForXp(5);
		int ranged = getLevelForXp(4);
		int magic = getLevelForXp(6);
		double base = 0.25 * (defence + hp + Math.floor(prayer / 2));
		double meleeC = 0.325 * (attack + strength);
		double rangeC = 0.325 * (Math.floor(ranged / 2) + ranged);
		double mageC = 0.325 * (Math.floor(magic / 2) + magic);
		return (int) Math.floor(base + Math.max(meleeC, Math.max(rangeC, mageC)));
	}

	public int getSummoningCombatLevel() {
		return getLevelForXp(Skills.SUMMONING) / 8;
	}

	public void drainSummoning(int amt) {
		int level = getLevel(Skills.SUMMONING);
		if (level == 0) {
			return;
		}
		setLevel(Skills.SUMMONING, amt > level ? 0 : level - amt);
	}

	public int getLevel(int skill) {
		return level[skill];
	}

	public void setLevel(int skill, int newLevel, boolean... refresh) {
		level[skill] = (short) newLevel;
		if (refresh.length == 0) {
			refresh(skill);
		} else if (refresh.length >= 1 && refresh[0]) {
			refresh(skill);
		}
	}

	public void refreshAllSkills() {
		for (int skill = 0; skill < level.length; skill++) {
			refresh(skill);
		}
	}

	public void init() {
		for (int skill = 0; skill < level.length; skill++) {
			refresh(skill);
		}
		refreshEnabledSkillsTargets();
		refreshUsingLevelTargets();
		refreshSkillsTargetsValues();
		refreshXpCounter();
	}

	private void refreshXpCounter() {
		player.getPackets().sendConfig(1801, (int) (xpCounter * 10D));
	}

	public void addXpNoModifier(int skill, double exp) {
		trackExperienceChange(skill, exp);
	}

	private void trackExperienceChange(int skill, double exp) {
		player.getControllerManager().trackXP(skill, exp);
		double modifier = player.getControllerManager().getExperienceModifier(skill, exp);
		if (modifier != -1) {
			exp = exp * modifier;
		}
		if (player.getFacade().isExperienceLocked()) {
			return;
		}
		if (player.getAuraManager().usingWisdom()) {
			exp *= 1.025;
		}
		int oldLevel = getLevelForXp(skill);
		double oldExp = getXp(skill);
		xp[skill] += exp;
		xpCounter += exp;
		refreshXpCounter();
		if (xp[skill] > MAXIMUM_EXP) {
			xp[skill] = MAXIMUM_EXP;
		}
		int newLevel = getLevelForXp(skill);
		double newExp = getXp(skill);
		int levelDiff = newLevel - oldLevel;
		if (newLevel > oldLevel) {
			level[skill] += levelDiff;
			player.getDialogueManager().startDialogue(LevelUp.class, skill);
			if (skill == SUMMONING || (skill >= ATTACK && skill <= MAGIC)) {
				player.getAppearence().generateAppearenceData();
				if (skill == HITPOINTS) {
					player.heal(levelDiff * 10);
				} else if (skill == PRAYER) {
					player.getPrayer().restorePrayer(levelDiff * 10);
				}
			}
		}
		if (oldExp < 200_000_000 && newExp >= 200_000_000) {
			World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Global</col>: " + player.getDisplayName() + " has just reached 200M experience in " + SKILL_NAME[skill] + "!", false);
		}
		refresh(skill);
	}

	public double getXp(int skill) {
		return xp[skill];
	}

	/**
	 * This method adds experience to the player's skills plus the modifiers for them. These modifiers include
	 * experience rates, player mode rates, and more
	 *
	 * @param skill
	 * 		The skill
	 * @param exp
	 * 		The amount of exp to add
	 */
	public void addXp(int skill, double exp) {
		// exp rate modifying
		switch (skill) {
			case ATTACK:
			case STRENGTH:
			case DEFENCE:
			case HITPOINTS:
			case MAGIC:
			case RANGE:
				if (getLevelForXp(skill) < 99) {
					// ults have 1x experience
					exp = exp * (player.isUltimateIronman() ? GameConstants.IRONMAN_COMBAT_EXP_RATE : combatRate == -1 ? GameConstants.COMBAT_EXP_RATE : combatRate);
				} else {
					exp = exp * 10;
				}
				break;
			case PRAYER:
				exp = exp * (player.isUltimateIronman() ? GameConstants.IRONMAN_PRAYER_EXP_RATE : GameConstants.PRAYER_EXP_RATE);
				break;
			// skills
			default:
				// ults have 1x experience
				exp = exp * (player.isUltimateIronman() ? skill == SUMMONING ? 2 : GameConstants.IRONMAN_SKILL_EXP_RATE : skill == SUMMONING ? 5 : GameConstants.SKIll_EXP_RATE);
				exp = exp * (player.isAnyDonator() ? 1.15 : 1);
				break;
		}

		// double experience
		if (DXPAlgorithms.isDoubleExperienceOn()) {
			double multiplier = DXPAlgorithms.getBonusExperience(player);
			double newExp = exp * multiplier;
			double bonus = newExp - exp;

			bonusExperienceGained += bonus;
			sendBonusConfigs();
			exp = newExp;
		} else if (skill == DailyEvents.getDailySkill()) {
			// gives them their exp bonus for the daily skill
			exp = exp * GameConstants.DAILY_EXP_BONUS;
		} else if (Wilderness.isAtWild(player) && player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
			exp *= player.getBrawlingGlovesManager().getExperienceModifier(skill);
		} else if (DXPAlgorithms.enabledHourlyBonus(player)) {
			exp = exp * 2;
		}
		trackExperienceChange(skill, exp);
	}

	public void addSkillXpRefresh(int skill, double xp) {
		this.xp[skill] += xp;
		restoreSkill(skill);
	}

	public void sendBonusConfigs() {
		sendBonusExperienceEnabled(true);
		int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(player.getFacade().getTimeSpentSinceDate(GameConstants.DOUBLE_EXPERIENCE_TIMES[0], player.getSignInTime()));
		sendBonusExperienceElapsed(minutes);
		sendBonusExperienceGained(bonusExperienceGained);
		refreshBonusExperience();
		//System.out.println("exp=" + exp + ", newExp=" + newExp + ", multiplier=" + multiplier);
	}

	public void resetSkillNoRefresh(int skill) {
		xp[skill] = 0;
		level[skill] = 1;
	}

	public void setXp(int skill, double exp, boolean... refresh) {
		xp[skill] = exp;
		if (refresh.length == 0) {
			refresh(skill);
		} else if (refresh.length >= 1 && refresh[0]) {
			refresh(skill);
		}
	}

	public int getTotalLevel() {
		int total = 0;
		for (short aLevel : level) {
			total += aLevel;
		}
		return total;
	}

	public long getTotalExp() {
		long exp = 0;
		for (double element : xp) {
			exp += element;
		}
		return exp;
	}

	/**
	 * Gets the amount of skills the player has maxed
	 */
	public int getAmountSkillsMaxed() {
		int amount = 0;
		for (int i = 0; i < SKILL_NAME.length; i++) {
			if (getLevelForXp(i) == (i == DUNGEONEERING ? 120 : 99)) {
				amount++;
			}
		}
		return amount;
	}

	/**
	 * Gets the skillcape to give the player based on the skill
	 *
	 * @param skill
	 * 		The skill
	 */
	public Item[] getSkillCape(int skill) {
		boolean shouldReceiveTrimmed = player.isAnyDonator() || getAmountSkillsMaxed() >= 5;
		String capeName = SKILL_NAME[skill];
		if (skill == CONSTRUCTION) {
			capeName = "Construct.";
		}
		if (skill == RANGE) {
			capeName = "Ranging";
		}
		if (skill == RUNECRAFTING) {
			capeName = "Runecraft";
		}
		if (shouldReceiveTrimmed) {
			switch (skill) {
				case WOODCUTTING:
					capeName = "Woodcut.";
					break;
			}
		}
		Item[] rewards = new Item[2];
		if (shouldReceiveTrimmed) {
			for (Item[] element : SkillSelectionInteractionEvent.TRIMMED_CAPES) {
				String name = element[0].getDefinitions().getName().toLowerCase();
				if (name.split(" ")[0].contains(capeName.toLowerCase())) {
					System.arraycopy(element, 0, rewards, 0, rewards.length);
				}
			}
		} else {
			for (Item[] element : SkillSelectionInteractionEvent.UNTRIMMED_CAPES) {
				String name = element[0].getDefinitions().getName().toLowerCase();
				if (name.split(" ")[0].contains(capeName.toLowerCase())) {
					System.arraycopy(element, 0, rewards, 0, rewards.length);
				}
			}
		}
		return rewards;
	}

	/**
	 * This method loops through all skills excluding dungeoneering and makes sure that the player has 99 in them. If
	 * they don't have 99, we return that skill which they have not maxed yet.
	 *
	 * @return The skill id they haven't maxed.
	 */
	public int isMaxed() {
		for (int i = 0; i < getLevels().length; i++) {
			if (i == DUNGEONEERING || i == CONSTRUCTION || i == FARMING) {
				continue;
			}
			if (getLevelForXp(i) != 99) {
				return i;
			}
		}
		return -1;
	}

	public short[] getLevels() {
		return level;
	}

	public void resetXpCounter() {
		xpCounter = 0;
		refreshXpCounter();
	}

	/**
	 * Sends the bonus experience enabled flag to the client
	 *
	 * @param enabled
	 * 		If it is enabled
	 */
	public void sendBonusExperienceEnabled(boolean enabled) {
		player.getVarsManager().sendVarBit(7232, enabled ? 1 : 0);
	}

	/**
	 * Sends the amount of hours that have elapsed since bonus experience was on
	 *
	 * @param seconds
	 * 		The seconds that have elapsed
	 */
	public void sendBonusExperienceElapsed(int seconds) {
		player.getVarsManager().sendVarBit(7233, seconds);
	}

	/**
	 * Sends the amount of bonus experience the player has gained
	 *
	 * @param experienceGained
	 * 		The experience gained
	 */
	public void sendBonusExperienceGained(double experienceGained) {
		player.getPackets().sendConfig(1878, (int) (experienceGained * 10));
	}

	/**
	 * Refreshes the bonus experience icon
	 */
	public void refreshBonusExperience() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendRunScript(1180);
				player.getPackets().sendRunScript(1160);
				for (int i = 0; i < 2; i++) { player.getPackets().sendRunScript(811, i); }
			}
		}, 1);
	}

    public int getCombatRate() {
        return this.combatRate;
    }

    public void setCombatRate(int combatRate) {
        this.combatRate = combatRate;
    }
}
