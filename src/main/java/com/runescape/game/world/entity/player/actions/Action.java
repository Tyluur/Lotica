package com.runescape.game.world.entity.player.actions;

import com.runescape.game.world.entity.player.Player;

public abstract class Action {

	public abstract boolean start(Player player);

	public abstract boolean process(Player player);

	public abstract int processWithDelay(Player player);

	public abstract void stop(Player player);

	protected final void setActionDelay(Player player, int delay) {
		player.getActionManager().setActionDelay(delay);
	}

	public boolean handleClick(Player player, int interfaceId, int componentId) { return false; }
}
