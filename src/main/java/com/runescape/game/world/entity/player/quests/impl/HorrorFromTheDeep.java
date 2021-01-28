package com.runescape.game.world.entity.player.quests.impl;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.ConfirmationDialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.QuestTag;
import com.runescape.utility.world.ClickOption;

import java.util.Arrays;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 15, 2015
 */
@QuestTag(HorrorFromTheDeep.Stages.class)
public class HorrorFromTheDeep extends Quest<HorrorFromTheDeep.Stages> {

	@Override
	public String getName() {
		return "Horror from the Deep";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by speaking to Jossik", "north of edgeville.", "", "This is a combat-only quest, so bring pvm supplies." };
		}
		switch (stage) {
			case FIGHTING:
				return new String[] { "Speak with Jossik to kill the Dagannoth Mother.", "Be prepared for a fight!" };
			case FINISHED:
				return new String[] { "<str>Speak with Jossik to kill the Dagannoth Mother.", "<str>Be prepared for a fight!", "", "Quest Complete!", "Awarded " + getQuestPoints() + " quest point!" };
			default:
				return new String[] { };
		}
	}

	@Override
	public void onStart(Player player) {
		player.getDialogueManager().startDialogue(new StartFightD());
	}

	@Override
	public void onFinish(Player player) {
		player.getControllerManager().forceStop();
	}

	@Override
	public int getQuestPoints() {
		return 1;
	}

	@Override
	public String getRewardInformation() {
		return "Access to the God Books";
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
						player.getDialogueManager().startDialogue(new CompletedQuestD(), npc.getId());
						break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the item id that will be sent over the completion interface
	 */
	@Override
	public int getQuestCompletionItemId() {
		return 3842;
	}

	@Override
	public boolean handleItemEquipping(Player player, int itemId) {
		Stages stage = getStage(player);
		int[] itemIds = { 19613, 19615, 19617 };
		boolean isBook = Arrays.stream(itemIds).anyMatch(value -> value == itemId);
		if (isBook && (stage == null || stage != Stages.FINISHED)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, QUEST_DIALOGUE_NPC_ID, "You don't have access to this book yet.", "Please speak to me first and complete Horror from the Deep.");
			return false;
		}
		return true;
	}

	/**
	 * @author Tyluur
	 */
	private final class StartFightD extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("Enter the battle?", "Yes", "No.");
		}

		@Override
		public void run(int interfaceId, int option) {
			if (option == FIRST) {
				player.getControllerManager().startController("HorrorFromTheDeepC");
			}
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
			player.getQuestManager().startQuest(HorrorFromTheDeep.class);
		}
	}

	private final class CompletedQuestD extends Dialogue {

		int npcId;

		@Override
		public void start() {
			sendNPCDialogue(npcId = getParam(0), CALM_TALKING, "You have completed my quest", "so I can sell you my rare books");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendPlayerDialogue(HAPPY, "Great! Can I see your stock?");
					stage = 0;
					break;
				case 0:
					sendNPCDialogue(npcId, CALM_TALKING, "Yes, sure.");
					stage = 1;
					break;
				case 1:
					end();
					openStore("Jossik's Book Shop");
					break;
			}
		}

		@Override
		public void finish() {

		}
	}

	public enum Stages {
		FIGHTING,
		FINISHED
	}

	/**
	 * The npc id of the quest dialogue npc
	 */
	private static final int QUEST_DIALOGUE_NPC_ID = 1334;

	/**
	 * The tile players end up at after the fight
	 */
	public static final WorldTile END_TILE = new WorldTile(3096, 3512, 0);
}
