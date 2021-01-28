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
 * @since Apr 18, 2015
 */
@QuestTag(RecipeForDisaster.Stages.class)
public class RecipeForDisaster extends Quest<RecipeForDisaster.Stages> {

	/**
	 * The key for the amount of monsters in rfd you killed
	 */
	public static final String KILLED_KEY = "monsters_killed";

	public static final int QUEST_DIALOGUE_NPC_ID = 3385;

	@Override
	public String getName() {
		return "Recipe for Disaster";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { new QuestRequirement("Level 60 Strength", player.getSkills().getLevelForXp(Skills.STRENGTH) >= 60), new QuestRequirement("Level 59 Magic", player.getSkills().getLevelForXp(Skills.MAGIC) >= 59), new QuestRequirement("1 Quest Point", player.getQuestManager().getPoints() >= 1), };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by speaking to the gypsy", "north of edgeville.", "", "This is a combat-only quest, so bring pvm supplies." };
		}
		switch (stage) {
			case STARTING:
			case FIGHTING:
				return new String[] { "Speak with the Gypsy to fight more monsters.", "You have killed " + getMonstersKilled(player) + " monsters now." };
			case ENDED:
				return new String[] { "<str>Speak with the Gypsy to fight more monsters.", "<str>You have killed " + getMonstersKilled(player) + " monsters now.", "", "Quest Complete!", "Awarded " + getQuestPoints() + " quest points!" };
			default:
				return new String[] { };
		}
	}

	@Override
	public void onStart(Player player) {
		storeAttribute(player, KILLED_KEY, 0D);
		storeAttribute(player, STAGE_KEY, Stages.FIGHTING);
		player.getDialogueManager().startDialogue(new StartFightD(), QUEST_DIALOGUE_NPC_ID);
	}

	@Override
	public void onFinish(Player player) {
		player.getDialogueManager().startDialogue(new CompletedQuestD(), QUEST_DIALOGUE_NPC_ID);
		player.getInventory().addItemDrop(4447, 1);
		//(SimpleNPCMessage.class, QUEST_DIALOGUE_NPC_ID, "You have completed my quest!", "You are now worthy to enter the nightmare zone.");
	}

	@Override
	public int getQuestPoints() {
		return 2;
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
					case STARTING:
						player.getDialogueManager().startDialogue(new StartFightD(), npcId);
						break;
					case ENDED:
						player.getDialogueManager().startDialogue(new CompletedQuestD(), npcId);
						break;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String getRewardInformation() {
		return "A double experience lamp.";
	}

	@Override
	public int getQuestCompletionItemId() {
		return 4447;
	}

	/**
	 * Gets the amount of monsters the player has killed
	 *
	 * @param player
	 * 		The player
	 */
	private int getMonstersKilled(Player player) {
		return player.getQuestManager().getAttribute(RecipeForDisaster.class, RecipeForDisaster.KILLED_KEY, 0D).intValue();
	}

	/**
	 * @author Tyluur
	 */
	private final class CompletedQuestD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = (Integer) parameters[0];
			sendNPCDialogue(npcId, HAPPY, "Congratulations on the battle.", "I hope you enjoyed the lamp!");
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
	private final class StartFightD extends Dialogue {

		int npcId;

		@Override
		public void start() {
			npcId = (Integer) parameters[0];
			sendOptionsDialogue("Are you ready to battle?", "Yes", "No");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (option) {
				case FIRST:
					player.getControllerManager().startController("RecipeForDisasterC");
					sendNPCDialogue(npcId, HAPPY, "Good luck!");
					stage = -2;
					break;
				case SECOND:
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
	private final class StartQuestD extends ConfirmationDialogue {

		@Override
		public void onConfirm() {
			player.getQuestManager().startQuest(RecipeForDisaster.class);
		}
	}

	public enum Stages {
		STARTING,
		FIGHTING,
		ENDED
	}
}
