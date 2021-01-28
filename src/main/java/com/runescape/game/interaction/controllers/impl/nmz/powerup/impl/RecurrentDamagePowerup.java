package com.runescape.game.interaction.controllers.impl.nmz.powerup.impl;

import com.runescape.game.interaction.controllers.impl.nmz.powerup.NMZPowerup;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/23/2016
 */
public class RecurrentDamagePowerup extends NMZPowerup {

	@Override
	public String name() {
		return "Recurrent Damage";
	}

	@Override
	public String activationMessage() {
		return "You activate the '" + name().toLowerCase() + "' powerup and feel full of energy.";
	}

	@Override
	public String depletionMessage() {
		return null;
	}

	@Override
	public String powerupKey() {
		return "recurrent_damage_powerup";
	}

	@Override
	public void onPickup(Player player) {
		super.onPickup(player);
		player.getCombatDefinitions().setSpecialAttack(Integer.MAX_VALUE);
	}

	@Override
	public void onDeplete(Player player) {
		player.getCombatDefinitions().setSpecialAttack(100);
	}

	@Override
	public int getObjectId() {
		return 14050;
	}

	@Override
	public long timeEffective() {
		return -1;
	}
}
