package com.runescape.game.content.skills.dungeoneering.rooms;

import com.runescape.game.content.skills.dungeoneering.DungeonManager;
import com.runescape.game.content.skills.dungeoneering.RoomReference;

public interface RoomEvent {

	void openRoom(DungeonManager dungeon, RoomReference reference);
}
