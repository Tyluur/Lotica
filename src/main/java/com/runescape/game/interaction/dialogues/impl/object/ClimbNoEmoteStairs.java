package com.runescape.game.interaction.dialogues.impl.object;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;
import com.runescape.network.codec.decoders.handlers.ObjectHandler;

public class ClimbNoEmoteStairs extends Dialogue {

	private WorldObject object;

	@Override
	public void start() {
		object = getParam(0);
		sendOptionsDialogue("What would you like to do?", "Go up the stairs.", "Go down the stairs.", "Never mind.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == 2) {
			ObjectHandler.climbUpStairs(player, object);
		} else if (componentId == 3) {
			ObjectHandler.climbDownStairs(player, object);
		}
		end();
	}

	@Override
	public void finish() {

	}

}
