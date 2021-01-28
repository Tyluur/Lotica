package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.content.skills.slayer.SlayerTasks;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class VannakaD extends Dialogue {

	NPC npc;

	int npcId;

	int startStage = 0;

	@Override
	public void start() {
		npc = (NPC) parameters[0];
		npcId = npc.getId();
		if (parameters.length == 2) {
			startStage = (Integer) parameters[1];
		}
		if (startStage != 0) {
			sendOptionsDialogue("Select a Task Type", "Easy", "Medium", "Hard", "Elite");
			stage = 0;
		} else {
			sendNPCDialogue(npcId, CASUAL, "Hello, how may I be of assistance?");
		}
	}

	/**
	 * Sends the task selecting dialogue
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 */
	public static void sendTaskDialogue(Player player, NPC npc) {
		player.getDialogueManager().startDialogue(VannakaD.class, npc, 1);
	}

	/**
	 * Sends the dialogue that tells the player they've received a new task
	 *
	 * @param player
	 * 		The player
	 */
	public static void receivedNewTaskDialogue(Player player) {
		player.getDialogueManager().startDialogue(new DialogueExtension());
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Stop social slayer", "Receive a task");
				stage = 3;
				break;
			case 0:
				SlayerTasks type = null;
				switch (option) {
					case FIRST:
						type = SlayerTasks.EASY;
						break;
					case SECOND:
						type = SlayerTasks.MEDIUM;
						break;
					case THIRD:
						type = SlayerTasks.HARD;
						break;
					case FOURTH:
						type = SlayerTasks.ELITE;
						break;
				}
				if (type == null) {
					throw new IllegalStateException();
				}
				if (player.getFacade().hasSlayerTask()) {
					sendNPCDialogue(npcId, CASUAL, "You already have a task to complete.", "Is this too challenging for you? I can give you a new one.", "", "This will cost " + Utils.numberToCashDigit(COST_FOR_TASK) + ", though.");
					player.putAttribute("new_task_type", type);
					stage = 1;
					return;
				}
				end();
				SlayerManagement.giveTask(player, type);
				break;
			case 1:
				sendOptionsDialogue("Pay " + Utils.numberToCashDigit(COST_FOR_TASK) + " for a new task?", "Yes", "No");
				stage = 2;
				break;
			case 2:
				if (option == FIRST) {
					if (player.takeMoney(COST_FOR_TASK)) {
						SlayerManagement.giveTask(player, player.getAttribute("new_task_type", SlayerTasks.EASY));
					} else {
						sendPlayerDialogue(ANGRY, "I'm too broke to afford this right now...");
						stage = -2;
					}
				} else {
					end();
				}
				break;
			case 3:
				if (option == FIRST) {
					if (player.getFacade().getSlayerPartner() != null) {
						player.getFacade().setSlayerPartner(null);
						Player partner = player.getFacade().getSlayerPartnerPlayer();
						if (partner != null) {
							SlayerManagement.removeSocialSlayer(player);
						}
						sendNPCDialogue(npcId, CALM, "Your social slayer has been turned off.");
					} else {
						sendNPCDialogue(npcId, CALM, "You don't have anybody you are doing social slayer with.");
					}
				} else {
					sendTaskDialogue(player, npc);
					return;
				}
				stage = -2;
				break;
		}
	}

	@Override
	public void finish() {
	}

	/**
	 * The cost for a new task
	 */
	private static final int COST_FOR_TASK = 150_000;

	/**
	 * @author Tyluur
	 */
	private static final class DialogueExtension extends Dialogue {

		@Override
		public void start() {
			sendItemDialogue(4155, 1, "You have recieved a new slayer task!", "You must kill " + player.getFacade().getSlayerTask().getAmountToKill() + "x " + player.getFacade().getSlayerTask().getName() + ".");
		}

		@Override
		public void run(int interfaceId, int option) {
			end();
		}

		@Override
		public void finish() {
		}
	}
}
