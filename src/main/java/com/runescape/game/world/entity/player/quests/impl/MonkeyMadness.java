package com.runescape.game.world.entity.player.quests.impl;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.ConfirmationDialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.QuestTag;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
@QuestTag(MonkeyMadness.Stages.class)
public class MonkeyMadness extends Quest<MonkeyMadness.Stages> {

	@Override
	public String getName() {
		return "Monkey Madness";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { new QuestRequirement("Level 25 Agility", player.getSkills().getLevelForXp(Skills.AGILITY) >= 25) };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by speaking to Garkor", "north of edgeville.", "", "This is a combat-only quest, so bring pvm supplies." };
		}
		switch (stage) {
			case FIGHTING:
				return new String[] { "Ready for the biggest fight of your life?", "Speak to Garkor to head over!" };
			case FINISHED:
				return new String[] { "<str>Ready for the biggest fight of your life?", "<str>Speak to Garkor to head over!", "", "Quest Complete!", "Rewarded " + getQuestPoints() + " quest points!" };
		}
		return null;
	}

	@Override
	public void onStart(Player player) {
		sendFightRequestDialogue(player);
	}

	/**
	 * Sends the dialogue to the player asking if they're ready to fight
	 *
	 * @param player
	 * 		The player
	 */
	private void sendFightRequestDialogue(Player player) {player.getDialogueManager().startDialogue(new StartFightD(), "Start the Monkey Madness boss fight?", "This fight is SAFE!", "<col=3399FF>You will not lose any items on death.", 19784);}

	@Override
	public void onFinish(Player player) {
		player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 1411, "Congratulations on the fight! Speak to me for some neat items.");
	}

	@Override
	public int getQuestPoints() {
		return 2;
	}

	@Override
	public String getRewardInformation() {
		return "Korasi Sword & Greegree Access";
	}

	@Override
	public int getQuestCompletionItemId() {
		return 19784;
	}

	@Override
	public boolean handleNPCInteract(Player player, NPC npc, ClickOption option) {
		final int npcId = npc.getId();
		if (npcId == QUEST_DIALOGUE_NPC_ID) {
			Stages stage = getStage(player);
			if (stage == null) {
				player.getDialogueManager().startDialogue(new StartQuestD(), "Do you wish to start " + getName() + "?", "Press Yes to start the quest.", "This is a safe quest - your items will be kept upon death.", getQuestCompletionItemId());
			} else {
				switch (stage) {
					case FIGHTING:
						sendFightRequestDialogue(player);
						break;
					case FINISHED:
						player.getDialogueManager().startDialogue(new ShopSelectD(npcId));
						break;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean handleItemEquipping(Player player, int itemId) {
		Stages stage = getStage(player);
		if (itemId == 19784 && stage != Stages.FINISHED) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, QUEST_DIALOGUE_NPC_ID, "You must complete the 'Monkey Madness' quest to have access to this blade.", "Check your quest diary to see how to start this quest.");
			return false;
		}
		return true;
	}

	/**
	 * The npc id of the npc that we speak to
	 */
	public static final int QUEST_DIALOGUE_NPC_ID = 1411;

	/**
	 * @author Tyluur
	 */
	private final class ShopSelectD extends Dialogue {

		/**
		 *
		 */
		private final int npcId;

		private ShopSelectD(int npcId) {
			this.npcId = npcId;
		}

		@Override
		public void start() {
			sendNPCDialogue(npcId, HAPPY, "Congratulations in the battle!", "Do you wish to view my shop now?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, shop please.", "No, never mind.");
					stage = 0;
					break;
				case 0:
					if (option == FIRST) {
						GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, "Monkey Madness Rewards"));
					}
					end();
					break;
			}
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * @author Tyluur
	 */
	private final class StartFightD extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("Start the boss fight?", "Yes, I am ready", "No");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (option) {
				case FIRST:
					player.getControllerManager().startController("MonkeyMadnessC");
					break;
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
			player.getQuestManager().startQuest(MonkeyMadness.class);
		}
	}

	public enum Stages {
		FIGHTING,
		FINISHED
	}
}
