package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.construction.House.RoomReference;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.Utils;

public class RemoveRoomD extends Dialogue {

	private RoomReference room;

	@Override
	public void start() {
		this.room = (RoomReference) parameters[0];
		sendOptionsDialogue("Remove the " + Utils.formatPlayerNameForDisplay(room.getRoom().toString()) + "?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == FIRST) {
			player.getHouse().removeRoom(room);
		}
		end();
	}

	@Override
	public void finish() {
	}

}
