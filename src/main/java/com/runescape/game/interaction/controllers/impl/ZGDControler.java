package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.content.global.minigames.ZarosGodwars;
import com.runescape.game.interaction.controllers.Controller;

public class ZGDControler extends Controller {

	@Override
	public void start() {
		ZarosGodwars.addPlayer(player);
		sendInterfaces();
	}

	@Override
	public boolean logout() {
		ZarosGodwars.removePlayer(player);
		return false; // so doesnt remove script
	}

	@Override
	public boolean login() {
		ZarosGodwars.addPlayer(player);
		sendInterfaces();
		return false; // so doesnt remove script
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().sendTab(player.getInterfaceManager().onResizable() ? 34 : 8, 601);
	}

	@Override
	public boolean sendDeath() {
		remove();
		removeController();
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		remove();
		removeController();
	}

	@Override
	public void forceClose() {
		remove();
	}

	public void remove() {
		ZarosGodwars.removePlayer(player);
		player.getPackets().closeInterface(player.getInterfaceManager().onResizable() ? 34 : 8);
	}
}
