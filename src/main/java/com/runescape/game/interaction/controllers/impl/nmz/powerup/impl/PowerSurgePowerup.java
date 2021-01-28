package com.runescape.game.interaction.controllers.impl.nmz.powerup.impl;

import com.runescape.game.interaction.controllers.impl.nmz.powerup.NMZPowerup;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/24/2016
 */
public class PowerSurgePowerup extends NMZPowerup {

	@Override
	public String name() {
		return "Power Surge";
	}

	@Override
	public String activationMessage() {
		return "You activate '" + name().toLowerCase() + "' and feel your special attack energy surged with life.";
	}

	@Override
	public String depletionMessage() {
		return "Your special attack energy has died down to regular power.";
	}

	@Override
	public String powerupKey() {
		return "power_surge_powerup";
	}

	@Override
	public int getObjectId() {
		return 14053;
	}

	@Override
	public long timeEffective() {
		return TimeUnit.SECONDS.toMillis(30);
	}
}
