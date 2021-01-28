package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class DeathInteractionEvent extends NPCInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 14386 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {

			int npcId;

			@Override
			public void start() {
				sendNPCDialogue(npcId = getParam(0), CALM, "Hello, I am death... What do you need mortal?");
			}

			@Override
			public void run(int interfaceId, int option) {
				switch (stage) {
					case -1:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Buy back lost untradeables", "Nothing, never mind.");
						stage = 0;
						break;
					case 0:
						if (option == FIRST) {
							if (player.getLostUntradeables().size() == 0) {
								sendNPCDialogue(npcId, CALM, "You have nothing to buy back from me.");
								stage = -2;
							} else {
								sendOptionsDialogue("Pay " + Utils.numberToCashDigit(getLostItemsCost(player)) + " for untradeables?", "Yes", "No.");
								stage = 1;
							}
						} else {
							end();
						}
						break;
					case 1:
						if (player.takeMoney(getLostItemsCost(player))) {
							player.getLostUntradeables().forEach(item -> player.getInventory().addItemDrop(item.getId(), item.getAmount()));
							player.getLostUntradeables().clear();
							sendNPCDialogue(npcId, CALM, "I expect to see you again soon...");
						} else {
							sendPlayerDialogue(CALM, "I don't have that much money right now.");
						}
						stage = -2;
						break;

				}
			}

			@Override
			public void finish() {

			}
		}, npc.getId());
		return true;
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

	private static final double LOST_ITEM_COST_MULTIPLIER = 2.5;
}
