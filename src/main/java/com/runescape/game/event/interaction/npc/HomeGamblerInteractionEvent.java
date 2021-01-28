package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/27/2015
 */
public class HomeGamblerInteractionEvent extends NPCInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 3001 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {

			int npcId;

			@Override
			public void start() {
				sendNPCDialogue(npcId = getParam(0), CALM, "Hello, how can I help you today?");
			}

			@Override
			public void run(int interfaceId, int option) {
				switch (stage) {
					case -1:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Buy a dice bag", "Buy 100 Mithril Seeds");
						stage = 0;
						break;
					case 0:
						switch (option) {
							case FIRST:
								sendOptionsDialogue("Dice bag costs " + Utils.numberToCashDigit(DICE_BAG_COST) + " gp, buy?", "Yes", "No");
								stage = 1;
								break;
							case SECOND:
								sendOptionsDialogue("100 Mithril seeds cost " + Utils.numberToCashDigit(1_250_000) + " gp, buy?", "Yes", "No");
								stage = 2;
								break;
						}
						break;
					case 1:
						if (player.takeMoney(DICE_BAG_COST)) {
							player.getInventory().addItemDrop(15098, 1);
							sendNPCDialogue(npcId, HAPPY, "Good luck out there!");
						} else {
							sendPlayerDialogue(CALM, "I don't have " + Utils.numberToCashDigit(DICE_BAG_COST) + " gold on me right now, sorry...");
						}
						stage = -2;
						break;
					case 2:
						if (player.takeMoney(MITHRIL_SEEDS_COST)) {
							player.getInventory().addItemDrop(299, 100);
							sendNPCDialogue(npcId, HAPPY, "Good luck out there!");
						} else {
							sendPlayerDialogue(CALM, "I don't have " + Utils.numberToCashDigit(1_250_000) + " gold on me right now, sorry...");
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

	/** The cost of a dice bag */
	private static final int DICE_BAG_COST = 2_500_000;

	/** The cost of 100 mithril seeds */
	private static final int MITHRIL_SEEDS_COST = 1_250_000;
}
