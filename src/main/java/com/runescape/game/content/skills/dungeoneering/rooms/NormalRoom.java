package com.runescape.game.content.skills.dungeoneering.rooms;

import com.runescape.game.content.skills.dungeoneering.DungeonManager;
import com.runescape.game.content.skills.dungeoneering.RoomReference;

public final class NormalRoom extends HandledRoom {

	private int complexity;

	public NormalRoom(int complexity, int chunkX, int chunkY,
			int... doorsDirections) {
		super(chunkX, chunkY, new RoomEvent() {
			@Override
			public void openRoom(DungeonManager dungeon, RoomReference reference) {
				dungeon.spawnRandomNPCS(reference);
			}
		}, doorsDirections);
		this.complexity = complexity;
	}

	@Override
	public boolean isComplexity(int complexity) {
		return this.complexity <= complexity;
	}

}
