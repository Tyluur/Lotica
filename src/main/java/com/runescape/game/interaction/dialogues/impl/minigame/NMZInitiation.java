package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/28/2015
 */
public class NMZInitiation extends Dialogue {

	@Override
	public void start() {
		sendDialogue("This door leads to the Nightmare Zone Lobby, however it is not free.", "You must pay 100K coins. Are you sure you wish to continue?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Pay & Enter", "Cancel");
				stage = 0;
				break;
			case 0:
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
