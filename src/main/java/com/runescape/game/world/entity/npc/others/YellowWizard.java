package com.runescape.game.world.entity.npc.others;

import com.runescape.game.interaction.controllers.impl.RunespanControler;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

@SuppressWarnings("serial")
public class YellowWizard extends NPC {
	
	private RunespanControler controler;
	private long spawnTime;
	public YellowWizard(WorldTile tile, RunespanControler controler) {
		super(15430, tile, -1, true, true);
		spawnTime = Utils.currentTimeMillis();
		this.controler = controler;
	}
	
	@Override
	public void processNPC() {
		if(spawnTime + 300000 < Utils.currentTimeMillis()) 
			finish();
	}
	
	@Override
	public void finish() {
		controler.removeWizard();
		super.finish();
	}
	public static void giveReward(Player player) {
		
	}
	@Override
	public boolean withinDistance(Player tile, int distance) {
		return tile == controler.getPlayer() && super.withinDistance(tile, distance);
	}

}
