package com.runescape.game.interaction.controllers.impl.nmz.powerup.impl;

import com.runescape.game.interaction.controllers.impl.nmz.NMZController;
import com.runescape.game.interaction.controllers.impl.nmz.monster.NMZMonster;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.NMZPowerup;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.player.Player;

import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/24/2016
 */
public class UltimateForcePowerup extends NMZPowerup {

	@Override
	public String name() {
		return "Ultimate Force";
	}

	@Override
	public String activationMessage() {
		return "There seems to be an ultimate force power unleashed in the room!";
	}

	@Override
	public String depletionMessage() {
		return null;
	}

	@Override
	public String powerupKey() {
		return "force_powerup";
	}

	@Override
	public void onPickup(Player player) {
		super.onPickup(player);
		player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> {
			List<NMZMonster> monsters = nmz.getInstance().getMonsterGenerator().getMonsters();
			for (NMZMonster monster : monsters) {
				monster.applyHit(new Hit(player, monster.getMaxHitpoints(), HitLook.REGULAR_DAMAGE));
			}
		});
	}

	@Override
	public int getObjectId() {
		return 14168;
	}

	@Override
	public long timeEffective() {
		return -1;
	}
}
