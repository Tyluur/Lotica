package com.runescape.game.world.entity.npc.familiar.impl;

import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

public class Smokedevil extends Familiar {

	private static final long serialVersionUID = -2734031002616044128L;

	public Smokedevil(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public String getSpecialName() {
		return "Dust Cloud";
	}

	@Override
	public String getSpecialDescription() {
		return "Hit up to 80 damage to all people within 1 square of you.";
	}

	@Override
	public int getBOBSize() {
		return 0;
	}

	@Override
	public int getSpecialAmount() {
		return 6;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.ENTITY;
	}

	@Override
	public boolean submitSpecial(Object object) {
		getOwner().setNextGraphics(new Graphics(1316));
		getOwner().setNextAnimation(new Animation(7660));
		setNextAnimation(new Animation(7820));
		setNextGraphics(new Graphics(1470));
		for (Entity entity : this.getPossibleTargets()) {
			if (entity == null || entity == getOwner() || !entity.withinDistance(this, 1)) { continue; }
			entity.applyHit(new Hit(this, Utils.random(80), HitLook.MAGIC_DAMAGE));
		}
		return true;
	}
}
