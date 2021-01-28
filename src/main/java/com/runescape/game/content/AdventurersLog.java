package com.runescape.game.content;

import com.runescape.game.world.entity.player.Player;

public final class AdventurersLog {

	private AdventurersLog() {
		
	}
	
	public static void open(Player player) {
		player.getInterfaceManager().sendInterface(623);
	}
}
