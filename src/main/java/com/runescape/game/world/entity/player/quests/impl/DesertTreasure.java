package com.runescape.game.world.entity.player.quests.impl;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.ConfirmationDialogue;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.QuestTag;
import com.runescape.utility.world.ClickOption;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 3, 2015
 */
@QuestTag(DesertTreasure.Stages.class)
public class DesertTreasure extends Quest<DesertTreasure.Stages> {

	@Override
	public String getName() {
		return "Desert Treasure";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { new QuestRequirement("Level 53 Thieving", player.getSkills().getLevelForXp(Skills.THIEVING) >= 53), new QuestRequirement("Level 50 Magic", player.getSkills().getLevelForXp(Skills.MAGIC) >= 50), new QuestRequirement("Level 25 Firemaking", player.getSkills().getLevelForXp(Skills.FIREMAKING) >= 25) };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by talking to the archaeologist", "north of edgeville.", "", "This is a combat-only quest, so bring pvm supplies." };
		}
		switch (stage) {
			case FIGHTING:
				return new String[] { "Speak to the archaeologist again to fight the bosses." };
			case FINISHED:
				return new String[] { "<str>Speak to the archaeologist again to fight the bosses.</str>", "", "Quest Complete!", "Awarded " + getQuestPoints() + " quest points." };
			default:
				return new String[] { };
		}
	}

	@Override
	public void onStart(Player player) {
		storeAttribute(player, MONSTERS_KEY, getMonsterProgress(player));
		storeAttribute(player, STAGE_KEY, Stages.FIGHTING);
		player.getDialogueManager().startDialogue(new StartFightD(), QUEST_DIALOGUE_NPC_ID);
	}

	@Override
	public int getQuestPoints() {
		return 1;
	}

	@Override
	public String getRewardInformation() {
		return "Access to Miasmic Spells.";
	}

	@Override
	public boolean handleNPCInteract(Player player, NPC npc, ClickOption option) {
		final int npcId = npc.getId();
		if (npcId == QUEST_DIALOGUE_NPC_ID) {
			Stages stage = getStage(player);
			if (stage == null) {
				player.getDialogueManager().startDialogue(new StartQuestD(), "Do you wish to start " + getName() + "?", "Press Yes to start the quest.", "This is a dangerous quest - you will lose items upon death!", getQuestCompletionItemId());
			} else {
				switch (stage) {
					case FIGHTING:
						player.getDialogueManager().startDialogue(new StartFightD());
						break;
					case FINISHED:
						player.getDialogueManager().startDialogue(new CompletedQuestD(npcId));
						break;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int getQuestCompletionItemId() {
		return 4675;
	}

	public static Map<String, Boolean> getMonsterProgress(Player player) {
		return player.getQuestManager().getAttribute(DesertTreasure.class, MONSTERS_KEY, new HashMap<>());
	}

	/**
	 * Updates the monster progress
	 *
	 * @param player
	 * 		The player
	 * @param index
	 * 		The index to update
	 * @param newValue
	 * 		The new value in the index
	 */
	public static void updateMonsterProgress(Player player, int index, boolean newValue) {
		Map<String, Boolean> monsterProgress = getMonsterProgress(player);
		monsterProgress.put("" + index, newValue);
		player.getQuestManager().addAttribute(DesertTreasure.class, MONSTERS_KEY, monsterProgress);
	}

	/**
	 * Finding out if we have killed the boss
	 *
	 * @param player
	 * 		The player to check for
	 * @param npcId
	 * 		The npc id of the boss
	 */
	public static boolean killedBoss(Player player, int npcId) {
		int index = getBossIndex(npcId);
		if (index == -1) {
			throw new IllegalStateException();
		}
		Map<String, Boolean> progress = getMonsterProgress(player);
		for (Iterator<Entry<String, Boolean>> it$ = progress.entrySet().iterator(); it$.hasNext(); ) {
			Entry<String, Boolean> entry = it$.next();
			if (entry.getKey().equalsIgnoreCase(String.valueOf(index))) {
				return entry.getValue();
			}
		}
		return false;
	}

	/**
	 * Gets the index of the boss in the {@link #BOSS_NPC_IDS} array
	 *
	 * @param npcId
	 * 		The id of the boss
	 */
	public static int getBossIndex(int npcId) {
		for (int i = 0; i < BOSS_NPC_IDS.length; i++) {
			int npcIds = BOSS_NPC_IDS[i];
			if (npcIds == npcId) {
				return i;
			}
		}
		if (npcId == 1975) {
			return 3;
		}
		return -1;
	}

	/**
	 * The array of boss npc ids
	 */
	private static final int[] BOSS_NPC_IDS = new int[] { 1914, 1913, 1977, 1974 };

	/**
	 * The key in the saved map for killed monsters
	 */
	public static final String MONSTERS_KEY = "killed_monsters";

	/**
	 * The tile the player is teleported to after the npc has been killed or they have died.
	 */
	public static final WorldTile FINISH_TILE = new WorldTile(3095, 3512, 0);

	/**
	 * The npc id of the quest dialogue npc
	 */
	private static final int QUEST_DIALOGUE_NPC_ID = 1918;

	/**
	 * @author Tyluur
	 */
	private final class StartFightD extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("Select Boss", "Dessous", "Kamil", "Fareed", "Damis");
		}

		@Override
		public void run(int interfaceId, int option) {
			end();
			int npcId = -1;
			switch (option) {
				case FIRST:
					npcId = BOSS_NPC_IDS[0];
					break;
				case SECOND:
					npcId = BOSS_NPC_IDS[1];
					break;
				case THIRD:
					npcId = BOSS_NPC_IDS[2];
					break;
				case FOURTH:
					npcId = BOSS_NPC_IDS[3];
					break;
			}
			player.getControllerManager().startController("DesertTreasureC", npcId);
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * @author Tyluur
	 */
	private final class CompletedQuestD extends Dialogue {

		/**
		 *
		 */
		private final int npcId;

		private CompletedQuestD(int npcId) {
			this.npcId = npcId;
		}

		@Override
		public void start() {
			sendNPCDialogue(npcId, CALM_TALKING, "You have completed this quest", "You now have access to my rare shop", "and the more powerful ancient magics spellbook.");
		}

		@Override
		public void run(int interfaceId, int option) {
			end();
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * @author Tyluur
	 */
	private final class StartQuestD extends ConfirmationDialogue {

		@Override
		public void onConfirm() {
			player.getQuestManager().startQuest(DesertTreasure.class);
		}
	}

	public enum Stages {
		FIGHTING,
		FINISHED
	}
}
