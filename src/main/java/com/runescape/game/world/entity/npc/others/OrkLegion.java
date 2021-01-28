package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/22/2016
 */
public class OrkLegion extends NPC {

	public OrkLegion(int id, WorldTile tile, Entity target) {
		super(id, tile);
		getCombat().setTarget(target);
		getCombat().process();
		this.setForceAgressive(true);
		this.setIntelligentRouteFinder(true);
		setSpawned(true);
	}

}
