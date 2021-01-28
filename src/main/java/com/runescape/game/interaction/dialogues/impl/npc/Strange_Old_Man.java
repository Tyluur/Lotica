package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.item.ItemDegrading;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Strange_Old_Man extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(npcId = (Integer) parameters[0], CALM, "I can repair all the broken items you're carrying", "but for a fee. Do you wish to do this?");
	}

	@Override
	public void run(int interfaceId, int option) {
		int cost = ItemDegrading.getRepairCost(player);
		switch(stage) {
		case -1:
			if (cost <= 0) {
				sendNPCDialogue(npcId, ANGRY, "You have nothing for me to repair...");
				stage = -2;
			} else {
				sendOptionsDialogue("Pay " + Utils.numberToCashDigit(cost)+ " to repair all items?", "Yes, please.", "No, cancel.");
				stage = 0;
			}
			break;
		case 0:
			if (option == FIRST){ 
				if (!player.takeMoney(cost)) {
					sendPlayerDialogue(ANGRY, "Stupid me, I'm too broke to afford this!");
				} else {
					ItemDegrading.repairAll(player);
					sendPlayerDialogue(HAPPY, "Thank you!");
				}
				stage = -2;
			} else {
				end();
			}
			break;
		}
	}

	@Override
	public void finish() {
	}
	
	int npcId;

}
