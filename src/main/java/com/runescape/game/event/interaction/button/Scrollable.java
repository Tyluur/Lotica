package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class Scrollable extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] {};
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		return false;
	}

	/**
	 * Sends text over the scroll interface which has a maximum of 67 allowed
	 * lines in it and automatically formats the text to fit the lines
	 *
	 * @param player
	 *            The player to display the interface to
	 * @param title
	 *            The title of the interface
	 * @param messageList
	 *            The list of messages to write onto the interface.
	 */
	public static void sendScroll(Player player, String title, String... messageList) {
		player.closeInterfaces();
		int interfaceId = 1142;
		String text = "";
		int entries = 0;
		for (String message : messageList) {
			if (entries++ >= 66) {
				break;
			}
			text += message + "<br>";
		}
		player.getPackets().sendIComponentText(interfaceId, 2, title);
		player.getPackets().sendIComponentText(interfaceId, 5, text);
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	/**
	 * Sends the quest interface to the player with the parameterized title and
	 * list of messages. The messages will be formatted to never overlap one
	 * line, but to go to the next one if it passes the limit of characters on a
	 * line.
	 * 
	 * @param player
	 *            The player
	 * @param title
	 *            The title of the quest interface
	 * @param messageList
	 *            The list of messages to send. a {@code String} {@code Array}
	 *            {@code Object}
	 */
	public static void sendQuestScroll(Player player, String title, String... messageList) {
		final int interfaceId = 275;
		final int endLine = 309;
		player.closeInterfaces();
		Utils.clearInterface(player, interfaceId);

		int startLine = 16;
		for (String message : messageList) {
			if (startLine > endLine) {
				break;
			}
			player.getPackets().sendIComponentText(interfaceId, startLine, message);
			startLine++;
		}

		player.getPackets().sendRunScript(1207, messageList.length);
		player.getPackets().sendIComponentText(interfaceId, 2, title);
		player.getInterfaceManager().sendInterface(interfaceId);
	}

}
