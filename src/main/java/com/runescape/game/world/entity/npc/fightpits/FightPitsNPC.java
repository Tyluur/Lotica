package com.runescape.game.world.entity.npc.fightpits;

import com.runescape.game.content.global.minigames.FightPits;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class FightPitsNPC extends NPC {


	public FightPitsNPC(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setForceMultiArea(true);
		setNoDistanceCheck(true);
	}
	
	@Override
	public void sendDeath(Entity source) {
		setNextGraphics(new Graphics(2924 + getSize()));
		super.sendDeath(source);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for(Player player : FightPits.arena)
			possibleTarget.add(player);
		return possibleTarget;
	}

}
