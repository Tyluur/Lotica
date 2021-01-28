package com.runescape.game.world.entity.npc.fightcaves;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.utility.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Yt_HurKot extends FightCavesNPC {

	private TzTok_Jad jad;

	private int nexHealTick;

	public Yt_HurKot(TzTok_Jad tzTok_Jad, int id, WorldTile tile) {
		super(id, tile);
		this.jad = tzTok_Jad;
		setRun(true);
		setForceAgressive(false);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (jad == null || jad.isDead()) {
			finish();
			return;
		}
		if (!isUnderCombat()) {
			calcFollow(jad, true);
			if (jad.getHitpoints() == jad.getMaxHitpoints() || !Utils.isOnRange(this, jad, 6)) {
				return;
			}
			nexHealTick++;
			if (nexHealTick % 2 == 0) {
//				jad.setNextGraphics(new Graphics(2992, 0, 300));
				setNextAnimation(new Animation(9254));
				setNextGraphics(new Graphics(444));
			}
			if (nexHealTick % 5 == 0) {// Approx 3 seconds
				setNextFaceEntity(jad);
				jad.heal((int) (jad.getMaxHitpoints() * .03));
			}
		}
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		return super.getPossibleTargets(false, true);
	}
}
