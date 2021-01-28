package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.interaction.dialogues.Dialogue;


public class SimplePlayerMessage extends Dialogue {

	@Override
	public void start() {
		String[] messages = new String[parameters.length];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = (String) parameters[i];
		}
		sendPlayerDialogue(CALM, messages);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {

	}

}
