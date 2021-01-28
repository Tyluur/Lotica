package com.runescape.game.world.entity.npc.others;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 7/5/2016
 */
public class MageArenaNPC extends NPC {

	public MageArenaNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() != HitLook.MAGIC_DAMAGE) {
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public void drop() {
		super.drop();
		Player killer = getMostDamageReceivedSourcePlayer();
		if (killer != null) {
			killer.getInventory().addItemDrop(Wilderness.WILDERNESS_TOKEN, 5);
		}
	}
}
