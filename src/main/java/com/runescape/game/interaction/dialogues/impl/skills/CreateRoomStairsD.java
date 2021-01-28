package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.construction.House.RoomReference;
import com.runescape.game.content.skills.construction.HouseConstants.Builds;
import com.runescape.game.content.skills.construction.HouseConstants.Room;
import com.runescape.game.interaction.dialogues.Dialogue;

public class CreateRoomStairsD extends Dialogue {

	private RoomReference room;
	private boolean up;

	@Override
	public void start() {
		this.room = (RoomReference) parameters[0];
		up = (boolean) parameters[1];
		sendOptionsDialogue("These stairs do not lead anywhere. Do you want to build a room at the " + (up ? "top" : "bottom") + "?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == FIRST) {
				stage = 0;
				if (room.getZ() == 1 && !up)
					sendOptionsDialogue(DEFAULT_OPTIONS, "Skill hall", "Quest hall", "Dungeon stairs room");
				else
					sendOptionsDialogue(DEFAULT_OPTIONS, "Skill hall", "Quest hall");
				return;
			}
		} else {
			Room r = room.getZ() == 1 && !up && componentId == THIRD ? Room.DUNGEON_STAIRS : componentId == SECOND ? up ? Room.HALL_QUEST_DOWN : Room.HALL_QUEST : up ? Room.HALL_SKILL_DOWN : Room.HALL_SKILL;
			Builds stair = room.getZ() == 1 && !up && componentId == THIRD ? Builds.STAIRCASE_2 : componentId == SECOND ? up ? Builds.STAIRCASE_DOWN_1 : Builds.STAIRCASE_1 : up ? Builds.STAIRCASE_DOWN : Builds.STAIRCASE;
			RoomReference newRoom = new RoomReference(r, room.getX(), room.getY(), room.getZ() + (up ? 1 : -1), room.getRotation());
			int slot = room.getStaircaseSlot();
			if (slot != -1) {
				newRoom.addObject(stair, slot);
				player.getHouse().createRoom(newRoom);
			}
		}
		end();

	}

	@Override
	public void finish() {

	}

}
