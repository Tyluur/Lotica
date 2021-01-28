package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.interaction.dialogues.Dialogue;

public class ForumThreadId extends Dialogue {

	@Override
	public void start() {
		player.getInterfaceManager().sendChatBoxInterface(1100);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();

	}

	@Override
	public void finish() {

	}

}
