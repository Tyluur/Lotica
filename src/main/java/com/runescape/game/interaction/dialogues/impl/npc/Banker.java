package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.LentItemsLoader;

public class Banker extends Dialogue {

	/**
	 * The multiplier of regular lost items' cost
	 */
	private static final double LOST_ITEM_COST_MULTIPLIER = 2.5;

	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, 9827, "Good day, how may I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue("What would you like to say?", "I'd like to access my bank account, please.", "I'd like to check my PIN settings.", "I would like to claim my lent items", "Could I buy back my lost untradeables?");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				player.getBank().openBank();
			} else if (componentId == OPTION_2) {
				player.getPinManager().openSettingsScreen();
			} else if (componentId == THIRD) {
				end();
				LentItemsLoader.claimAllLentItems(player);
				return;
			} else if (componentId == FOURTH) {
				if (player.getLostUntradeables().size() == 0) {
					sendNPCDialogue(npcId, CALM, "You have nothing to buy back from me.");
					stage = -2;
				} else {
					sendOptionsDialogue("Pay " + Utils.numberToCashDigit(getLostItemsCost(player)) + " for untradeables?", "Yes", "No.");
					stage = 1;
				}
				return;
			}
			end();
		} else if (stage == 1) {
			if (player.takeMoney(getLostItemsCost(player))) {
				player.getLostUntradeables().forEach(item -> player.getInventory().addItemDrop(item.getId(), item.getAmount()));
				player.getLostUntradeables().clear();
				sendNPCDialogue(npcId, CALM, "I expect to see you again soon...");
			} else {
				sendPlayerDialogue(CALM, "I don't have that much money right now.");
			}
			stage = -2;
		}
	}

	/**
	 * Gets the total cost for the items we're buying back
	 *
	 * @param player
	 * 		The player we're checking the items of
	 */
	private int getLostItemsCost(Player player) {
		int cost = 0;
		for (Item item : player.getLostUntradeables()) {
			cost += getCostOfItem(item);
		}
		return cost;
	}

	@Override
	public void finish() {

	}

	/**
	 * Gets the cost of the items when bought back from death
	 *
	 * @param item
	 * 		The item we're finding the cost
	 */
	private int getCostOfItem(Item item) {
		String name = item.getName().toLowerCase();
		if (name.contains("void")) {
			return 800_000;
		} else if (name.contains("chaotic")) {
			return 1_000_000;
		} else if (name.contains("defender")) {
			return 250_000;
		}
		switch (item.getId()) {
			case 18335:
				return 750_000;
			case 18361:
			case 18363:
				return 900_000;
			case 19784:
				return 1_000_000;
		}
		return (int) (item.getDefinitions().getValue() * LOST_ITEM_COST_MULTIPLIER);
	}

}
