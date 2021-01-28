package com.runescape.game.interaction.controllers.impl.nmz;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public enum NMZModes {

	EASY(0.75), HARD(1.075), ELITE(1.145),

	;

	private final double pointModifier;

	NMZModes(double pointModifier) {
		this.pointModifier = pointModifier;
	}

	public double getPointModifier() {
		return pointModifier;
	}
}
