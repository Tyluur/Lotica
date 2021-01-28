package com.runescape.game.world.entity.npc.familiar.impl;

import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

public class Lavatitan extends Familiar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -778365732778023700L;

	public Lavatitan(Player owner, Pouches pouch, WorldTile tile,
			int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public String getSpecialName() {
		return "Ebon Thunder";
	}

	@Override
	public String getSpecialDescription() {
		return "Possibly drain an enemy's special attack energy by 10%";
	}

	@Override
	public int getBOBSize() {
		return 0;
	}

	@Override
	public int getSpecialAmount() {
		return 4;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.ENTITY;
	}

	@Override
	public boolean submitSpecial(Object object) {
		return false;
	}

}
