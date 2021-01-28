package com.runescape.game.world.entity.npc.familiar.impl;

import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

public class Thornysnail extends Familiar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1147053487269627345L;

	public Thornysnail(Player owner, Pouches pouch, WorldTile tile,
			int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public String getSpecialName() {
		return "Slime Spray";
	}

	@Override
	public String getSpecialDescription() {
		return "Inflicts up to 80 damage against your opponent.";
	}

	@Override
	public int getBOBSize() {
		return 3;
	}

	@Override
	public int getSpecialAmount() {
		return 0;
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
