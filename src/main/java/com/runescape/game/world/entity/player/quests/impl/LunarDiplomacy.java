package com.runescape.game.world.entity.player.quests.impl;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.ConfirmationDialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.QuestTag;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
@QuestTag(LunarDiplomacy.Stages.class)
public class LunarDiplomacy extends Quest<LunarDiplomacy.Stages> {

	@Override
	public String getName() {
		return "Lunar Diplomacy";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { new QuestRequirement("Level 40 Defence", player.getSkills().getLevelForXp(Skills.DEFENCE) >= 40) };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by speaking to the Ethereal Mimic", "north of edgeville.", "", "This is a combat-only quest, so bring pvm supplies." };
		}
		switch (stage) {
		case FIGHTING:
			return new String[] { "You must fight yourself to complete this quest.", "Speak to the Ethereal Mimic when you're ready." };
		case FINISHED:
			return new String[] { "<str>You must fight yourself to complete this quest.", "<str>Speak to the Ethereal Mimic when you're ready.", "", "Quest Complete!", "Rewarded " + getQuestPoints() + " quest points!" };
		}
		return null;
	}

	@Override
	public void onStart(Player player) {
		player.getDialogueManager().startDialogue(new StartFightD());
	}

	@Override
	public int getQuestPoints() {
		return 1;
	}

	@Override
	public String getRewardInformation() {
		return "Lunar Spellbook & Wardrobe Access";
	}

	@Override
	public boolean handleNPCInteract(Player player, NPC npc, ClickOption option) {
		int npcId = npc.getId();
		if (npcId == QUEST_DIALOGUE_NPC_ID) {
			Stages stage = getStage(player);
			if (stage == null) {
				player.getDialogueManager().startDialogue(new StartQuestD(), "Do you wish to start " + getName() + "?", "Press Yes to start the quest.", "This is a safe quest - your items will be kept upon death.", getQuestCompletionItemId());
			} else {
				switch (stage) {
				case FIGHTING:
					player.getDialogueManager().startDialogue(new StartFightD());
					break;
				case FINISHED:
					player.getDialogueManager().startDialogue(new FinishedQuestD(npcId));
					break;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int getQuestCompletionItemId() {
		return 9075;
	}

	/**
	 * The npc id of the npc that we speak to
	 */
	public static final int QUEST_DIALOGUE_NPC_ID = 33;

	/**
	 * @author Tyluur
	 *
	 */
	private final class FinishedQuestD extends Dialogue {

		private FinishedQuestD(int npcId) {
			this.npcId = npcId;
		}

		@Override
		public void start() {
			sendNPCDialogue(npcId, CALM_TALKING, "You have completed this quest.", "Would you like to see my supplies now?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch(stage) {
				case -1:
					sendOptionsDialogue(DEFAULT_OPTIONS, "View lunar wardrobe store.", "Cancel.");
					stage = 0;
					break;
				case 0:
					if (option == FIRST) {
						openStore("Lunar Supplies");
					}
					end();
					break;
			}
		}

		@Override
		public void finish() {
		}

		private int npcId;
	}

	/**
	 * @author Tyluur
	 *
	 */
	private final class StartFightD extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("Start the fight?", "Yes, I'm ready.", "No, I must prepare.");
		}

		@Override
		public void run(int interfaceId, int option) {
			end();
			switch (option) {
			case FIRST:
				player.getControllerManager().startController("LunarDiplomacyC");
				break;
			case SECOND:
				break;
			}
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * @author Tyluur
	 *
	 */
	private final class StartQuestD extends ConfirmationDialogue {

		@Override
		public void onConfirm() {
			player.getQuestManager().startQuest(LunarDiplomacy.class);
		}
	}

	public enum Stages {
		FIGHTING, FINISHED
	}

}
