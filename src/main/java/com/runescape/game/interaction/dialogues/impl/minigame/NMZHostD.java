package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.interaction.controllers.impl.nmz.NMZLobby;
import com.runescape.game.interaction.controllers.impl.nmz.NMZModes;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/28/2015
 */
public class NMZHostD extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), CALM, "Greetings, dark warrior, I am the dream", "host: your Nightmare Zone guide.", "How may I be of assistance to you?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Enter the nightmare zone", "View shop selection", "Check my points", "Cancel");
				stage = 0;
				break;
			case 0:
				switch (option) {
					case FIRST:
						sendOptionsDialogue("Select a Game Mode", "Easy", "Hard", "Elite");
						stage = 2;
						break;
					case SECOND:
						openStore("Nightmare Zone Shop");
						break;
					case THIRD:
						sendNPCDialogue(npcId, HAPPY, "Check your game points page to see your points.", "(located at: ::account -> game points)");
						stage = -2;
						break;
					case FOURTH:
						end();
						break;
				}
				break;
			case 2:
				NMZModes mode = null;
				switch (option) {
					case 2:
						mode = NMZModes.EASY;
						break;
					case 3:
						mode = NMZModes.HARD;
						break;
					case 4:
						mode = NMZModes.ELITE;
						break;
				}
				final NMZModes endMode = mode;
				player.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(lobby -> lobby.transfer(endMode));
				break;
		}

	}

	@Override
	public void finish() {

	}
}
