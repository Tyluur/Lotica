package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/3/2015
 */
public class RemoveBuildD extends Dialogue {

	private WorldObject object;

	@Override
	public void start() {
		object = getParam(0);
		sendOptionsDialogue("Really remove it?", "Yes", "No");
	}

	@Override
	public void run(int interfaceId, int option) {
		if (option == FIRST) {
			player.getHouse().removeBuild(object);
		}
		end();
	}

	@Override
	public void finish() {

	}
}
