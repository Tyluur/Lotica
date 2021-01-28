package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class Dream_Host extends Dialogue {

	int npcId;

	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), CALM, "Hello, I am the nightmare zone (NMZ) operator", "would you like to participate today?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendDialogue("This leads to the Nightmare Zone Lobby, however it is not free.", "You must pay 100K coins. Are you sure you wish to continue?");
				stage = 0;
				break;
			case 0:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Pay 100K coins to enter the lobby", "Never mind");
				stage = 1;
				break;
			case 1:
				if (option == FIRST) {
					if (player.takeMoney(100_000)) {
						player.getControllerManager().startController("NMZLobby");
					} else {
						sendPlayerDialogue(CALM, "I must return with 100K coins first.");
					}
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
