package com.runescape.game.world.entity.npc.nomad;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;

@SuppressWarnings("serial")
public class FakeNomad extends NPC {
	
	private Nomad nomad;
	
	public FakeNomad(WorldTile tile, Nomad nomad) {
		super(8529, tile, -1, true, true);
		this.nomad = nomad;
		setForceMultiArea(true);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		nomad.destroyCopy(this);
	}
	
}
