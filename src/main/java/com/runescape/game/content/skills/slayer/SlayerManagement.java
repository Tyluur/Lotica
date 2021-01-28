package com.runescape.game.content.skills.slayer;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.interaction.dialogues.impl.skills.VannakaD;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.hard.Hard_Slayer;
import com.runescape.game.world.entity.player.achievements.medium.Journey_Man_Slayer;
import com.runescape.game.world.entity.player.achievements.medium.Slayer_Hunter;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class SlayerManagement {

	/**
	 * Gives the player a random slayer task of a type
	 *
	 * @param player
	 * 		The player to receive the task
	 * @param type
	 * 		The type of task the player wants
	 */
	public static void giveTask(Player player, SlayerTasks type) {
		List<String> names = SlayerTasks.getTasksNames(type);
		if (names == null) {
			noAvailableTasks(player);
			return;
		}
		// so only the monsters that npcs with no definitions or npcs with
		// a required lvl lower than the players slayer lvl can be given
		names = names.stream().filter(name -> {
			SlayerMonsters monster = SlayerMonsters.getMonster(name);
			return monster == null || player.getSkills().getLevelForXp(Skills.SLAYER) >= monster.getRequirement();
		}).collect(Collectors.toList());

		if (names.size() == 0) {
			noAvailableTasks(player);
			return;
		}
		Collections.shuffle(names);
		String randomName = names.get(0);
		int[] amounts = type.getAmounts();
		int amount = Utils.random(amounts[0], amounts[1]);

		SlayerTask task = new SlayerTask(randomName, amount, type);
		player.getFacade().setSlayerTask(task);
		if (player.getFacade().getSlayerPartnerPlayer() != null) {
			player.getFacade().getSlayerPartnerPlayer().getFacade().setSlayerTask(task);
		}
		VannakaD.receivedNewTaskDialogue(player);
	}

	private static void noAvailableTasks(Player player) {
		player.getDialogueManager().startDialogue(SimpleItemMessage.class, 4155, "There are no tasks available for you", "at your current slayer level.", "Select a different type.");
	}

	/**
	 * Invites a player to be a player's social slayer partner
	 *
	 * @param player
	 * 		The player inviting
	 * @param p2
	 * 		The player invited
	 */
	public static void invitePlayer(Player player, Player p2) {
		player.putAttribute("social_request", p2);
		player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " an invitation...");
		p2.getPackets().sendMessage(117, "You have received an invitation to join " + player.getDisplayName() + "'s social slayer group.", player);
	}

	/**
	 * Views the invitation to be someones slayer partner
	 *
	 * @param player
	 * 		The player
	 * @param inviter
	 * 		The inviter
	 */
	public static boolean viewInvite(Player player, Player inviter) {
		if (inviter.removeAttribute("social_request") == player) {
			String message = availableForPartnering(player, inviter);
			if (message != null) {
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, 4155, message);
				inviter.getDialogueManager().startDialogue(SimpleItemMessage.class, 4155, message);
				return true;
			}
			player.getDialogueManager().startDialogue(new Dialogue() {
				@Override
				public void start() {
					sendOptionsDialogue("Be slayer partners with " + inviter.getDisplayName() + "?", "Yes", "No");
				}

				@Override
				public void run(int interfaceId, int option) {
					if (option == FIRST) {
						String localMessage = availableForPartnering(player, inviter);
						if (localMessage != null) {
							sendItemDialogue(4155, 1, localMessage);
							inviter.getDialogueManager().startDialogue(SimpleItemMessage.class, 4155, localMessage);
						} else {
							inviter.getFacade().setSlayerPartner(player.getUsername());
							inviter.getPackets().sendGameMessage("You have created a social group.");

							player.getFacade().setSlayerPartner(inviter.getUsername());
							sendDialogue("You have just joined " + inviter.getDisplayName() + "'s social group.");
						}
						stage = -2;
					} else {
						end();
					}
				}

				@Override
				public void finish() {

				}
			});
			return true;
		}
		return false;
	}

	/**
	 * Checks if the two players are available for partnering. If they aren't, the reason why is returned
	 *
	 * @param player
	 * 		The player
	 * @param inviter
	 * 		The player inviting
	 */
	public static String availableForPartnering(Player player, Player inviter) {
		SlayerTask slayerTask = player.getFacade().getSlayerTask();
		SlayerTask slayerTaskO = inviter.getFacade().getSlayerTask();
		if (slayerTask != null && slayerTaskO != null && !slayerTask.equals(slayerTaskO)) {
			return player.getDisplayName() + " needs to complete their slayer task before joining.";
		} else if (slayerTaskO != null && slayerTask != null && !slayerTaskO.equals(slayerTask)) {
			return inviter.getDisplayName() + " needs to complete their slayer task before joining.";
		} else if (player.getFacade().getSlayerPartner() != null) {
			return player.getDisplayName() + " needs to leave their current partner before joining a social slayer group.";
		} else if (inviter.getFacade().getSlayerPartner() != null) {
			return inviter.getDisplayName() + " needs to leave their current partner before joining a social slayer group.";
		}
		return null;
	}

	/**
	 * Reducing the task amount
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The monster that died
	 */
	public static void reduceTaskAmount(Player player, NPC npc) {
		Player partner = player.getFacade().getSlayerPartnerPlayer();
		player.getFacade().getSlayerTask().deductTaskAmount();
		if (partner != null && partner.withinDistance(player, 16) && partner.getFacade().hasSlayerTask() && partner.getFacade().getSlayerTask().equals(player.getFacade().getSlayerTask())) {
			partner.getFacade().getSlayerTask().deductTaskAmount();
			partner.getSkills().addXp(Skills.SLAYER, npc.getMaxHitpoints() / (equippingSlayerHelm(partner) ? 7 : 10));
			checkProgress(partner, npc);
		}
		player.getSkills().addXp(Skills.SLAYER, npc.getMaxHitpoints() / (equippingSlayerHelm(player) ? 7 : 10));
		checkProgress(player, npc);
	}

	/**
	 * Checks the progress of the slayer task
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The monster that died
	 */
	private static void checkProgress(Player player, NPC npc) {
		if (player.getFacade().hasSlayerTask() && player.getFacade().getSlayerTask().getAmountToKill() <= 0) {

			SlayerTasks task = player.getFacade().getSlayerTask().getTask();
			if (task != null) {
				if (task.equals(SlayerTasks.MEDIUM)) {
					AchievementHandler.incrementProgress(player, Slayer_Hunter.class);
					AchievementHandler.incrementProgress(player, Journey_Man_Slayer.class);
				} else if (task.equals(SlayerTasks.HARD)) {
					AchievementHandler.incrementProgress(player, Hard_Slayer.class);
				}
			}

			int points = player.getFacade().getSlayerTask().getTask().getSlayerPointReward();
			int[] coinLootData = player.getFacade().getSlayerTask().getTask().getCoinLoots();

			npc.sendDrop(player, new Item(995, Utils.random(coinLootData[0], coinLootData[1])), false);
			player.getFacade().setSlayerPoints(player.getFacade().getSlayerPoints() + points);

			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 1597, "You have completed your Slayer task. Come back to me for a new one.", "You receive " + points + " slayer points for completing this task.");
			player.getFacade().removeSlayerTask();
		}
	}

	/**
	 * Removes social slayer for this player and for the target if they're in the world.
	 *
	 * @param player
	 * 		The player
	 */
	public static void removeSocialSlayer(Player player) {
		Player partner = player.getFacade().getSlayerPartnerPlayer();
		player.getFacade().setSlayerPartner(null);
		if (partner != null) {
			partner.getFacade().setSlayerPartner(null);
			partner.sendMessage("Your partner has just turned off social slayer.");
		}
	}

	/**
	 * Checking if the player is equipping a slayer helm
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean equippingSlayerHelm(Player player) {return player.getEquipment().getHatId() == 15492;}

	/**
	 * Checking if the player is equipping a black mask
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean equippingBlackMask(Player player) {
		int hatId = player.getEquipment().getHatId();
		return hatId == 13263 || hatId == 14636 || hatId == 14637 || hatId == 15492 || hatId == 15496 || hatId == 15497 || hatId >= 8901 && hatId <= 8921;
	}

	/**
	 * Checks if the npc is the player's task
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 */
	public static boolean isTask(Player player, NPC npc) {
		return player.getFacade().hasSlayerTask() && npc.getName().toLowerCase().contains(player.getFacade().getSlayerTask().getName().toLowerCase());
	}
}
