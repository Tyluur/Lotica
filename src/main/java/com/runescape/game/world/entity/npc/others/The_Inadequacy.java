package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class The_Inadequacy extends NPC {

	public The_Inadequacy(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public void sendDeath(Entity source) {
		System.out.println("The_Inadequacy.sendDeath");
		if (getId() == 5904) {
			transformIntoNPC(5903);
			setHitpoints(getMaxHitpoints());
			System.out.println("died 1");
		} else if (getId() == 5903) {
			transformIntoNPC(5902);
			setHitpoints(getMaxHitpoints());
			System.out.println("died 2");
		} else {
			super.sendDeath(source);
			System.out.println("died fr");
		}
	}
}
