package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/3/2015
 */
public class Polypore_Store extends Dialogue {

	int npcId;
	
	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), CALM, "Hello adventurer, how may I be of help to you today?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Open your shop, please.", "Never mind...");
				stage = 0;
				break;
			case 0:
				if (option == FIRST) {
					openStore("Polypore Dungeon Supplies");
				}
				end();
				break;
		}
	}

	@Override
	public void finish() {

	}
}
