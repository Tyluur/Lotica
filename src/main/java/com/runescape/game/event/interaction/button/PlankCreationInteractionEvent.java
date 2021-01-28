package com.runescape.game.event.interaction.button;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class PlankCreationInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 403 };
	}

	/**
	 * Displays the interface to the player
	 * 
	 * @param player
	 *            The player
	 */
	public static void display(Player player) {
		int interfaceId = 403;
		for (Planks plank : Planks.values()) {
			player.getPackets().sendIComponentText(interfaceId, 20 + plank.ordinal(), Utils.formatPlayerNameForDisplay(plank.name()) + "<br>Cost = " + Utils.format(plank.getCost()) + "gp per log");
		}
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		Planks plank = (componentId == 12 ? Planks.WOOD : componentId == 13 ? Planks.OAK : componentId == 14 ? Planks.TEAK : componentId == 15 ? Planks.MAHOGANY : null);
		if (plank == null) {
			return true;
		}
		int amount = 0;
		switch (packetId) {
		case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
			amount = 1;
			break;
		case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
			amount = 5;
			break;
		case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
			amount = 10;
			break;
		case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
			player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {

				@Override
				public void handleInput() {
					makePlanks(player, plank, getInput());
				}
			});
			return true;
		case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
			amount = player.getInventory().getAmountOf(plank.getLogId());
			break;
		}
		if (amount <= 0) {
			return true;
		}
		makePlanks(player, plank, amount);
		return true;
	}

	/**
	 * Makes the amount of planks desired
	 * 
	 * @param player
	 *            The player
	 * @param plank
	 *            The plank instance
	 * @param amount
	 *            The amount to make
	 */
	private void makePlanks(Player player, Planks plank, int amount) {
		int logCount = player.getInventory().getAmountOf(plank.getLogId());
		if (amount > logCount) {
			amount = logCount;
		}
		if (!player.getInventory().containsItem(plank.getLogId(), amount)) {
			player.sendMessage("You don't have that many " + ItemDefinitions.forId(plank.getLogId()).name.toLowerCase() + "!");
			return;
		}
		int cost = plank.getCost() * amount;
		if (!player.takeMoney(cost)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 4250, "You need " + Utils.format(cost) + " coins to do this.");
			return;
		}
		player.getInventory().deleteItem(plank.getLogId(), amount);
		player.getInventory().addItem(plank.getPlankId(), amount);
	}

	public enum Planks {

		WOOD(960, 1511, 1250), OAK(8778, 1521, 5750), TEAK(8780, 6333, 8850), MAHOGANY(8782, 6332, 12500);

		Planks(int plankId, int logId, int cost) {
			this.plankId = plankId;
			this.logId = logId;
			this.cost = cost;
		}

		public static Planks forId(int id) {
			for (Planks plank : Planks.values()) {
				if (plank.getLogId() == id) {
					return plank;
				}
			}
			return null;
		}

		public int getPlankId() {
			return plankId;
		}

		public int getLogId() {
			return logId;
		}

		public int getCost() {
			return cost;
		}

		private final int plankId;
		private final int logId;
		private final int cost;
	}
}
