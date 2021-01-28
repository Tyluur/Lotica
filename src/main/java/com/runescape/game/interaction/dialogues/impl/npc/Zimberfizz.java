package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class Zimberfizz extends Dialogue {

	@Override
	public void start() {
		if (true) {
			end();
			return;
		}
		sendNPCDialogue(npcId = getParam(0), CALM, "Hey! I'm Zimberfizz - the Pyramid Hunting employee.", "I can teach you about the game or give you rewards.", "What would you like?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue("Select an Option", "Learn about Pyramid Hunting.", "Purchase " + GameConstants.SERVER_NAME + " Point Rewards", "How many " + GameConstants.SERVER_NAME + " Points do I have?", "Cancel");
				stage = 0;
				break;
			case 0:
				switch (option) {
					case FIRST:
						sendNPCDialogue(npcId, HAPPY, "Hehe I'm happy you asked!", "Pyramid hunting involves hunting down", "monsters to kill, and afterwards hunting down a treasure.", "You only have 3 lifes in here so be careful.");
						stage = 1;
						break;
					case SECOND:
						end();
						GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, "Pyramid Hunting Rewards"));
						break;
					case THIRD:
						sendNPCDialogue(npcId, CALM, "Check out your information tab to view point data like this!", "It has just been opened for you.");
						stage = -2;
						player.getInterfaceManager().openGameTab(1);
						break;
					case FOURTH:
						end();
						break;
				}
				break;
			case 1:
				sendNPCDialogue(npcId, CALM, "Use the red portal to player single-player, or", "enter the blue portal for team-mode with your clan.");
				stage = -2;
				break;
		}
	}

	@Override
	public void finish() {

	}


	int npcId;
}
