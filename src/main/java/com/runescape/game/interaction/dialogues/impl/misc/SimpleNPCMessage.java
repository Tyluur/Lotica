package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.interaction.dialogues.Dialogue;

public class SimpleNPCMessage extends Dialogue {

	@Override
	public void start() {
		int npcId = getParam(0);
		String[] messages = new String[parameters.length - 1];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = (String) parameters[i + 1];
		}
		sendNPCDialogue(npcId, 9827, messages);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {

	}

}
