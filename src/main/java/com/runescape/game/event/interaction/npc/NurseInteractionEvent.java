package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.interaction.dialogues.impl.npc.NurseD;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class NurseInteractionEvent extends NPCInteractionEvent {

	/**
	 * The delay between the time regular players can use the nurse (seconds)
	 */
	public static final int DELAY_BETWEEN = 300;

	@Override
	public int[] getKeys() {
		return new int[] { 961 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.FIRST) {
			player.getDialogueManager().startDialogue(NurseD.class, npc);
		} else if (option == ClickOption.SECOND) {
			healPlayer(player, npc);
			return true;
		}
		return true;
	}

	/**
	 * Heals the player
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 */
	public static boolean healPlayer(Player player, NPC npc) {
		if (!player.isAnyDonator()) {
			Object restoreObject = player.getFacade().getAttribute("last_restore_time");
			long lastRestoreTime = -1;
			if (restoreObject != null) {
				lastRestoreTime = formatNumber(restoreObject);
			}
			// we never stored
			long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastRestoreTime);
			if (lastRestoreTime == -1 || seconds > DELAY_BETWEEN) {
				player.getFacade().putAttribute("last_restore_time", System.currentTimeMillis());
			} else {
				player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 961, "You must wait " + (DELAY_BETWEEN - seconds) + " more seconds to use this feature.", "Donators are free from this limitation.");
				return true;
			}
		}
		/** Performing a cool npc interaction */
		npc.setNextAnimation(new Animation(12575));
		player.setNextGraphics(new Graphics(1314));
		/** Refreshing all player characteristics to optimal settings */
		player.restoreAll();
		/** Sending surgeon dialogue */
		player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 961, "I have restored your character to extreme health!");
		player.sendMessage("You feel refreshed, past your normal health levels.");
		return false;
	}

	/**
	 * Formats a number to a long
	 *
	 * @param value
	 * 		The number
	 */
	private static long formatNumber(Object value) {
		String string = value.toString();
		string = string.replace(".", "");
		if (string.contains("E")) { string = string.substring(0, string.indexOf("E")); }
		return Long.parseLong(string);
	}

}
