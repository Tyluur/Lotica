package com.runescape.game.content.skills.dungeoneering;

import com.runescape.game.world.entity.player.Player;

public class DungeonPartyPlayer {

	private Player player;
	private int deaths;

	public DungeonPartyPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void refreshDeaths() {
		player.getPackets().sendConfigByFile(7554, deaths);
	}

	public void increaseDeaths() {
		if (deaths == 15)
			return;
		deaths++;
		refreshDeaths();
	}
}
