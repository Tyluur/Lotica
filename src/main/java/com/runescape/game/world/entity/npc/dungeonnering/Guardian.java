package com.runescape.game.world.entity.npc.dungeonnering;

import com.runescape.game.content.skills.dungeoneering.DungeonManager;
import com.runescape.game.content.skills.dungeoneering.RoomReference;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;

@SuppressWarnings("serial")
public class Guardian extends NPC {

	private DungeonManager manager;
	private RoomReference reference;

	public Guardian(int id, WorldTile tile, DungeonManager manager,
			RoomReference reference) {
		super(id, tile, -1, true, true);
		this.manager = manager;
		this.reference = reference;
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		manager.updateGuardian(reference);
	}

}
