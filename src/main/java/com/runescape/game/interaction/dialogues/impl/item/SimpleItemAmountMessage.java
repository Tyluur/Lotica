package com.runescape.game.interaction.dialogues.impl.item;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 10/21/2015
 */
public class SimpleItemAmountMessage extends Dialogue {

	@Override
	public void start() {
		String[] text = new String[parameters.length - 2];
		for (int i = 0; i < parameters.length; i++) {
			if (i <= 1) {
				continue;
			}
			text[i - 2] = getParam(i);
		}
		sendItemDialogue(getParam(0), getParam(1), text);
	}

	@Override
	public void run(int interfaceId, int option) {
		end();
	}

	@Override
	public void finish() {

	}
}
