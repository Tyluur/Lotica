package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.controllers.impl.FightCaves;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.pet.RewardPet;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/25/2016
 */
public class FightCavesEntranceD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "Burn 5 fire capes for a TzRek-Jad", "Gamble your fire cape for at a 50% chance at a kiln cape", "Enter the fight caves");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				option = option - 1;
				switch(option)  {
					case FIRST:
						sendOptionsDialogue("Confirm you wish to burn 5 capes", "Yes", "No");
						stage = 1;
						break;
					case SECOND:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Confirm you wish to gamble your cape", "Cancel");
						stage = 0;
						break;
					case THIRD:
						FightCaves.enterFightCaves(player, false);
						break;
				}
				break;
			case 0:
				if (option == FIRST) {
					if (player.getInventory().containsItem(6570, 1)) {
						player.getInventory().deleteItem(6570, 1);
						if (Utils.percentageChance(25)) {
							sendItemDialogue(22346, 1, "You exchange your fire cape", "for a more powerful tokhaar-kal.");
							player.getInventory().addItem(22346, 1);
						} else {
							sendItemDialogue(6570, 1, "Your fire cape burns away.", "You are left with nothing.");
						}
					} else {
						sendPlayerDialogue(CALM, "I don't have a fire cape.");
					}
					stage = -2;
				} else {
					end();
				}
				break;
			case 1:
				if (option == FIRST) {
					if (player.getInventory().containsItem(6570, 5)) {
						player.getInventory().deleteItem(6570, 5);
						RewardPet.addPet(player, RewardPet.TZREK_JAD);
						sendItemDialogue(6570, 1, "You burn 5 fire capes for a TzRekJad");
					} else {
						sendItemDialogue(6570, 1, "You don't have 5 fire capes to burn");
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
}
