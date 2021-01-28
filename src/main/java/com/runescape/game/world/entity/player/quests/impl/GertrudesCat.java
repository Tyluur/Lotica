package com.runescape.game.world.entity.player.quests.impl;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.QuestTag;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
@QuestTag(GertrudesCat.Stages.class)
public class GertrudesCat extends Quest<GertrudesCat.Stages> {

	@Override
	public String getName() {
		return "Gertrude's Cat";
	}

	@Override
	public QuestRequirement[] getQuestRequirements(Player player) {
		return new QuestRequirement[] { };
	}

	@Override
	public String[] getJournalText(Player player) {
		Stages stage = getStage(player);
		if (stage == null) {
			return new String[] { "You can start this quest by speaking to Gertrude.", "She is West of Varrock..." };
		}
		switch (stage) {
			case STARTED:
				return new String[] { "Speak to Gertude, she will tell you what to do." };
			case SPEAKING_TO_CHILDREN:
				return new String[] { "I should go speak to Gertrude's children, she said", "they are somewhere around Varrock.", "Their names are Shilop and Wilough." };
			case FINDING_CAT:
				return new String[] { "I've been told where the children last saw Fluffs.", "She was near the Jolly Boar Inn...", "I should bring Fluffs something to drink." };
			case GAVE_FLUFFS_MILK:
				return new String[] { "Perhaps Fluffs is hungry?" };
			case GAVE_FLUFFS_SARDINE:
				return new String[] { "There are kittens very closeby, I should search around." };
			case FOUND_KITTENS:
				return new String[] { "I've found the kittens... I should give them to fluffs." };
			case GAVE_FLUFF_KITTENS:
				return new String[] { "Fluffs is on the way home to Gertrude. I'll go speak with her." };
			case FINISHED:
				return new String[] { "<str>Fluffs is on the way home to Gertrude. I'll go speak with her.</str>", "", "Quest Complete!", "Awarded " + getQuestPoints() + " quest point." };
			default:
				return new String[] { };
		}
	}

	@Override
	public void onStart(Player player) {
		player.getDialogueManager().startDialogue(new SpeakToChildrenD(780));
	}

	@Override
	public void onFinish(Player player) {
		player.getInventory().addItem(Utils.randomArraySlot(KITTENS), 1);
	}

	@Override
	public int getQuestPoints() {
		return 1;
	}

	@Override
	public String getRewardInformation() {
		return "Access to pets and a kitten.";
	}

	@Override
	public boolean handleNPCInteract(Player player, NPC npc, ClickOption cOption) {
		int npcId = npc.getId();
		Stages stage = getStage(player);
		switch (npcId) {
			case 7742: // fluffs
				player.getPackets().sendLocalNPCsUpdate();
				if (stage == null) {
					return false;
				}
				switch (stage) {
					case STARTED:
					case SPEAKING_TO_CHILDREN:
						return false;
					case FINDING_CAT:
						npc.setNextAnimation(new Animation(9163));
						npc.setNextForceTalk(new ForceTalk("Hisssssss"));
						player.getLockManagement().lockAll();
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								stop();
								player.getDialogueManager().startDialogue(SimpleMessage.class, "Maybe the cat is thirsty?");
								player.setNextForceTalk(new ForceTalk("Ouch!"));
								player.getLockManagement().unlockAll();
							}

						}, 3);
						break;
					case GAVE_FLUFFS_MILK:
						npc.setNextAnimation(new Animation(9163));
						npc.setNextForceTalk(new ForceTalk("Hisssssss"));
						player.getLockManagement().lockAll();
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								stop();
								player.getDialogueManager().startDialogue(SimpleMessage.class, "Maybe the cat is hungry?");
								player.setNextForceTalk(new ForceTalk("Ouch!"));
								player.getLockManagement().unlockAll();
							}

						}, 3);
						break;
					case GAVE_FLUFFS_SARDINE:
						npc.setNextAnimation(new Animation(9163));
						npc.setNextForceTalk(new ForceTalk("Hisssssss"));
						player.getLockManagement().lockAll();
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								stop();
								setCrateLocation(player);
								player.getDialogueManager().startDialogue(SimpleMessage.class, "The cat seems afraid to leave.", "In the distance you can hear kittens mewing...");
								player.setNextForceTalk(new ForceTalk("Ouch!"));
								player.getLockManagement().unlockAll();
							}

						}, 3);
						break;
					case FOUND_KITTENS:
						npc.setNextAnimation(new Animation(9163));
						npc.setNextForceTalk(new ForceTalk("Purr.."));
						player.getLockManagement().lockAll();
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								stop();
								setCrateLocation(player);
								player.setNextForceTalk(new ForceTalk("Purr.."));
								player.getLockManagement().unlockAll();
							}

						}, 3);
						break;
					case GAVE_FLUFF_KITTENS:
					case FINISHED:
						return false;
					default:
						break;
				}
				return true;
			case 783: // wilough
			case 781: // shilop
				if (stage == null) {
					return false;
				}
				switch (stage) {
					case STARTED:
						return false;
					case SPEAKING_TO_CHILDREN:
						player.getDialogueManager().startDialogue(new ChildrenFindD(), npcId);
						break;
					case FINDING_CAT:
						player.getDialogueManager().startDialogue(new CatInformationD(), npcId);
						break;
					case GAVE_FLUFFS_MILK:
					case GAVE_FLUFFS_SARDINE:
					case FOUND_KITTENS:
						player.getDialogueManager().startDialogue(new LookingForFluffsD(), npcId);
						break;
					case GAVE_FLUFF_KITTENS:
						player.getDialogueManager().startDialogue(new FoundFluffsD(), npcId);
						break;
					case FINISHED:
						player.getDialogueManager().startDialogue(new CompletedQuestD(), npcId);
						break;
				}
				return true;
			case 780: // gertrude
				if (stage == null) {
					player.getDialogueManager().startDialogue(new StartQuestD(npcId));
				} else {
					switch (stage) {
						case STARTED:
							player.getDialogueManager().startDialogue(new SpeakToChildrenD(npcId));
							break;
						case SPEAKING_TO_CHILDREN:
							player.getDialogueManager().startDialogue(new GoSpeakD(), npcId);
						case FINDING_CAT:
						case GAVE_FLUFFS_MILK:
						case GAVE_FLUFFS_SARDINE:
						case FOUND_KITTENS:
							player.getDialogueManager().startDialogue(new GoSpeakD(), npcId);
							break;
						case GAVE_FLUFF_KITTENS:
							player.getDialogueManager().startDialogue(new ClaimRewardsD(), npcId);
							break;
						case FINISHED:
							player.getDialogueManager().startDialogue(new CompletedQuestD(), npcId);
							break;
						default:
							break;
					}
				}
				return true;
		}
		return false;
	}

	@Override
	public boolean handleObjectInteract(Player player, WorldObject object, ClickOption option) {
		if (object.getId() == 2620) {
			if (getStage(player) != null && getStage(player) == Stages.GAVE_FLUFFS_SARDINE) {
				WorldTile targetLocation = getCrateLocation(player);
				if (targetLocation == null) {
					return false;
				}
				player.sendMessage("You search the crate...");
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						stop();
						if (object.getWorldTile().matches(targetLocation)) {
							player.getDialogueManager().startDialogue(SimpleMessage.class, "You find the kittens! You carefully place them in your backpack.");
							player.getInventory().addItem(13236, 1);
							setStage(player, Stages.FOUND_KITTENS);
						} else {
							player.sendMessage("You find nothing.");
							if (targetLocation.withinDistance(player, 5)) {
								player.sendMessage("You hear kittens mewing closeby.");
							}
						}
					}
				}, 2);
			} else {

			}
			return true;
		}
		return false;
	}

	@Override
	public int getQuestCompletionItemId() {
		return 1561;
	}

	/**
	 * @author Tyluur
	 */
	private final class ClaimRewardsD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendPlayerDialogue(HAPPY, "Hello Gertrude. Fluffs ran off with her kittens");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendNPCDialogue(npcId, HAPPY, "You're back! Thank you! Thank you! Fluffs just came back!", "I think she was upset as she couldn't find her kittens.");
					stage = 0;
					break;
				case 0:
					sendDialogue("Gertrude gives you a hug.");
					stage = 1;
					break;
				case 1:
					sendNPCDialogue(npcId, HAPPY, "If you hadn't found her kittens they", "would have died out there!");
					stage = 2;
					break;
				case 2:
					sendPlayerDialogue(CALM, "That's ok, I like to do my bit.");
					stage = 3;
					break;
				case 3:
					sendNPCDialogue(npcId, CALM, "I don't know how to thank you.", "I have no real material possessions.", "I do have kittens! I can only really look after one.");
					stage = 4;
					break;
				case 4:
					sendPlayerDialogue(CALM, "Well if it needs a home.");
					stage = 5;
					break;
				case 5:
					sendNPCDialogue(npcId, CALM, "I would sell it to my cousin in West Ardougne.", "I hear there's a rat epidemic there. But it's too far.");
					stage = 6;
					break;
				case 6:
					sendNPCDialogue(npcId, CALM, "Here you go, look after her and thank you again!");
					stage = 7;
					break;
				case 7:
					sendNPCDialogue(npcId, HAPPY, "Oh by the way, the kitten can live in your backpack", "but to make it grow you must take it out", "and feed and stroke it often.");
					stage = 8;
					break;
				case 8:
					sendDialogue("Gertrude gives you a kitten.");
					stage = 9;
					break;
				case 9:
					end();
					player.getQuestManager().finishQuest(GertrudesCat.class);
					break;
			}
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * Gets the location of the crate we should search
	 *
	 * @param player
	 * 		The player
	 */
	public WorldTile getCrateLocation(Player player) {
		return player.getQuestManager().getAttribute(getClass(), CRATE_LOCATION_KEY);
	}

	/**
	 * Sets the crate location
	 */
	private void setCrateLocation(Player player) {
		storeAttribute(player, CRATE_LOCATION_KEY, getRandomCrateLocation());
	}

	/**
	 * Finds a random crate location from the {@link #CRATE_LOCATIONS} array
	 */
	private WorldTile getRandomCrateLocation() {
		return Utils.randomArraySlot(CRATE_LOCATIONS);
	}

	/**
	 * The key for the location
	 */
	private static final String CRATE_LOCATION_KEY = "CRATE_LOCATION";

	/**
	 * The possible locations for crates
	 */
	private static final WorldTile[] CRATE_LOCATIONS = new WorldTile[] { new WorldTile(3310, 3513, 0), new WorldTile(3301, 3502, 0), new WorldTile(3304, 3500, 0), new WorldTile(3309, 3503, 0), new WorldTile(3311, 3500, 0), new WorldTile(3310, 3498, 0) };

	/**
	 * The possible kittens you can receive
	 */
	private static final Integer[] KITTENS = new Integer[] { 1555, 1556, 1557, 1558, 1559, 1560, 14089 };

	/**
	 * @author Tyluur
	 */
	private final class CompletedQuestD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendNPCDialogue(npcId, CALM, "Enjoy your kitten!");
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
	private final class FoundFluffsD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendNPCDialogue(npcId, CALM, "How's it going?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendPlayerDialogue(CALM, "I found fluffs!");
					stage = 0;
					break;
				case 0:
					sendNPCDialogue(npcId, CALM, "You should go tell my mom them, she would be really happy.");
					stage = -2;
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
	private final class LookingForFluffsD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendNPCDialogue(npcId, CALM, "How's it going with Fluffs?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendPlayerDialogue(CALM, "Is there anything else I should know about fluffs?");
					stage = 0;
					break;
				case 0:
					sendNPCDialogue(npcId, CALM, "She likes doodle sardines, I forgot to tell you that.", "They're a mixture of doodle leaves and raw sardines.");
					stage = 1;
					break;
				case 1:
					sendPlayerDialogue(CALM, "Where can I find them?");
					stage = 2;
					break;
				case 2:
					sendNPCDialogue(interfaceId, CALM, "I don't know that... Sorry sir. Look around my moms house for some doodle leaves.");
					stage = -2;
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
	private final class CatInformationD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendPlayerDialogue(CALM, "Now where did you last see Fluffs?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendNPCDialogue(npcId, CALM, "I played at an abandoned lumber mill to the north east.", "Just beyond the Jolly Boar Inn.", "I saw Fluffs running around in there.");
					stage = 2;
					break;
				case 2:
					sendPlayerDialogue(CALM, "Anything else?");
					stage = 3;
					break;
				case 3:
					sendNPCDialogue(npcId, THINKING_STILL, "Well, you have to find the broken fence to get in.", "I'm sure you can manage that.");
					stage = -2;
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
	private final class ChildrenFindD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendPlayerDialogue(CALM, "Hello there, I've been looking for you.");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendNPCDialogue(npcId, SAD, "I didn't mean to take it! I just forgot to pay.");
					stage = 0;
					break;
				case 0:
					sendPlayerDialogue(CALM, "What? I'm trying to help your mom find Fluffs.");
					stage = 1;
					break;
				case 1:
					sendNPCDialogue(npcId, CALM, "Ohh... well, in that case I might be able to help.", "Fluffs followed me to my secret play area.", "I haven't seen her since.");
					stage = 2;
					break;
				case 2:
					sendPlayerDialogue(CALM, "Where is this play area?");
					stage = 3;
					break;
				case 3:
					sendNPCDialogue(npcId, CALM, "If I told you, it wouldn't be a secret.");
					stage = 4;
					break;
				case 4:
					sendOptionsDialogue(DEFAULT_OPTIONS, "Tell me sonny, or I will hurt you.", "What will make you tell me?", "Well never mind, it's Fluffs' loss.");
					stage = 5;
					break;
				case 5:
					switch (option) {
						case 2:
							sendPlayerDialogue(CALM, "Tell me sonny, or I will hurt you.");
							stage = 6;
							break;
						case 3:
							sendNPCDialogue(npcId, CALM, "Well.. now that you mention it, I am a bit short on cash.");
							stage = 7;
							break;
						case 4:
							sendPlayerDialogue(CALM, "Forget it, it's Fluffs' loss anyways.");
							stage = -2;
							break;
					}
					break;
				case 6:
					sendNPCDialogue(npcId, ANGRY, "I'm telling my mom you said that!");
					stage = -2;
					break;
				case 7:
					sendPlayerDialogue(CALM, "How much?");
					stage = 8;
					break;
				case 8:
					sendNPCDialogue(npcId, CALM, "100 coins should cover it.");
					stage = 9;
					break;
				case 9:
					sendPlayerDialogue(ANGRY, "100 coins? Why should I pay you?");
					stage = 10;
					break;
				case 10:
					sendNPCDialogue(npcId, CALM, "You shouldn't, but I won't help otherwise.", "I never liked that can anyway. So what do you say?");
					stage = 11;
					break;
				case 11:
					sendOptionsDialogue(DEFAULT_OPTIONS, "I'm not paying you a penny.", "Okay then, I'll pay.");
					stage = 12;
					break;
				case 12:
					switch (option) {
						case FIRST:
							sendNPCDialogue(npcId, CALM, "I'm not paying you a penny.");
							stage = -2;
							break;
						case SECOND:
							sendPlayerDialogue(CALM, "Okay then, I'll pay.");
							stage = 13;
							break;
					}
					break;
				case 13:
					if (player.takeMoney(100)) {
						sendItemDialogue(995, 100, "You give the lad 100 coins...");
						stage = 14;
					} else {
						sendPlayerDialogue(ANGRY, "I'll be back when I have 100 coins for you.");
						stage = -2;
					}
					break;
				case 14:
					end();
					setStage(player, Stages.FINDING_CAT);
					player.getDialogueManager().startDialogue(new CatInformationD(), npcId);
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
	private final class GoSpeakD extends Dialogue {

		private int npcId;

		@Override
		public void start() {
			npcId = getParam(0);
			sendNPCDialogue(npcId, CALM, "Please go speak to my children... They were last playing with her.");
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
	private final class SpeakToChildrenD extends Dialogue {

		public SpeakToChildrenD(int npcId) {
			this.npcId = npcId;
		}

		@Override
		public void start() {
			sendNPCDialogue(npcId, SAD, "My, what a kind person you are.", "Please speak to my children, they", "are somewhere around varrock...");
		}

		@Override
		public void run(int interfaceId, int option) {
			sendNPCDialogue(npcId, SAD, "They were the ones who last had fluffs.", "Their names are Shilop and Wilough.");
			setStage(player, Stages.SPEAKING_TO_CHILDREN);
			stage = -2;
		}

		@Override
		public void finish() {
		}

		private int npcId;
	}

	/**
	 * @author Tyluur
	 */
	private final class StartQuestD extends Dialogue {

		public StartQuestD(int npcId) {
			this.npcId = npcId;
		}

		@Override
		public void start() {
			sendNPCDialogue(npcId, SAD, "Oh please, please help me!");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendPlayerDialogue(CALM, "What's wrong, ma'am?");
					stage = 0;
					break;
				case 0:
					sendNPCDialogue(npcId, SAD, "It's my cat, fluffs! Something must've happened to her...");
					stage = 1;
					break;
				case 1:
					sendPlayerDialogue(CALM, "Why do you say that?", "I can help you if you need anything.");
					stage = 2;
					break;
				case 2:
					sendNPCDialogue(npcId, EYES_WIDE, "Really? I love her so much and I can't find her.", "Could you find her for me?");
					stage = 3;
					break;
				case 3:
					sendOptionsDialogue(DEFAULT_OPTIONS, "Start \"Gertrude's Cat\"?", "Never mind.");
					stage = 4;
					break;
				case 4:
					switch (option) {
						case FIRST:
							end();
							player.getQuestManager().startQuest(GertrudesCat.class);
							break;
						case SECOND:
							sendPlayerDialogue(ANGRY, "I don't care about your cat!");
							stage = -2;
							break;
					}
					break;
			}
		}

		@Override
		public void finish() {
		}

		private int npcId;
	}

	public enum Stages {
		STARTED,
		SPEAKING_TO_CHILDREN,
		FINDING_CAT,
		GAVE_FLUFFS_MILK,
		GAVE_FLUFFS_SARDINE,
		FOUND_KITTENS,
		GAVE_FLUFF_KITTENS,
		FINISHED
	}

}
