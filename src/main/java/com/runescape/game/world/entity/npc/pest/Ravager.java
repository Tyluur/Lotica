package com.runescape.game.world.entity.npc.pest;

import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.world.WorldTile;

@SuppressWarnings("serial")
public class Ravager extends PestMonsters {

	boolean destroyingObject = false;

	public Ravager(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, int index, PestControl manager) {
		super(id, tile, -1, false, false, index, manager);
	}

	@Override
	public void processNPC() {
		super.processNPC();
	}
}
