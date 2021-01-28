package com.runescape.game.world.entity.npc.fightcaves;

import com.runescape.game.interaction.controllers.impl.FightCaves;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class TzTok_Jad extends FightCavesNPC {

	private boolean spawnedMinions;

	private FightCaves controler;
	
	public TzTok_Jad(int id, WorldTile tile, FightCaves controler) {
		super(id, tile);
		this.controler = controler;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (!spawnedMinions && getHitpoints() < getMaxHitpoints() / 2) {
			spawnedMinions = true;
			controler.spawnHealers(this);
		}
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
					setNextGraphics(new Graphics(2924 + getSize()));
				} else if (loop >= defs.getDeathDelay()) {
					reset();
					finish();
					controler.win();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}
