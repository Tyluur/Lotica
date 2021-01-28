package com.runescape.game.interaction.dialogues.impl.item;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class SimpleItemMessage extends Dialogue {

	@Override
	public void start() {
		int itemId = (Integer) parameters[0];
		String[] messages = new String[parameters.length - 1];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = (String) parameters[i + 1];
		}
		sendItemDialogue(itemId, 1, messages);
	}

	@Override
	public void run(int interfaceId, int option) {
		end();
	}

	@Override
	public void finish() {
	}

}
