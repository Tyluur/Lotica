package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.construction.House.RoomReference;
import com.runescape.game.interaction.dialogues.Dialogue;

public class CreateRoomD extends Dialogue {

	private RoomReference room;

	@Override
	public void start() {
		this.room = (RoomReference) parameters[0];
		sendPreview();
	}

	public void sendPreview() {
		sendOptionsDialogue("Select an Option", "Rotate clockwise", "Rotate anticlockwise.", "Build.", "Cancel");
		player.getHouse().previewRoom(room, false);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == FOURTH) {
			end();
			return;
		}
		if (componentId == THIRD) {
			end();
			player.getHouse().createRoom(room);
			return;
		}
		player.getHouse().previewRoom(room, true);
		room.setRotation((room.getRotation() + (componentId == FIRST ? 1 : -1)) & 0x3);
		sendPreview();
	}

	@Override
	public void finish() {
		player.getHouse().previewRoom(room, true);
	}

}
