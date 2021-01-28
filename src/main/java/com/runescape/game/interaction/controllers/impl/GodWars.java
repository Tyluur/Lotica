package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.godwars.Bosses;

public class GodWars extends Controller {

	@Override
	public void start() {
		player.removeAttribute("godwar_altar_uses");
		sendTab();
	}

	@Override
	public void process() {
		if (!player.getInterfaceManager().containsInterface(601)) {
			sendTab();
		}
	}

	@Override
	public void magicTeleported(int type) {
		removeOverlay();
		removeController();
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 57225) {
			player.getDialogueManager().startDialogue("NexEntrance");
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		removeOverlay();
		removeController();
		return true;
	}

	@Override
	public boolean login() {
		sendTab();
		return false; // so doesnt remove script
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void forceClose() {
		for (int i = 0; i < player.getFacade().getGwdKillcount().length; i++) {
			player.getFacade().getGwdKillcount()[i] = 0;
		}
		player.removeAttribute("godwar_altar_uses");
		removeOverlay();
	}

	public void removeOverlay() {
		player.getInterfaceManager().closeOverlay();
	}

	public void sendTab() {
		player.getInterfaceManager().sendOverlay(601);
		updateInterface();
	}

	public void updateInterface() {
		player.getVarsManager().sendVarBit(3939, player.getFacade().getGwdKillcount()[Bosses.ARMADYL.ordinal()]); // arma
		player.getVarsManager().sendVarBit(3941, player.getFacade().getGwdKillcount()[Bosses.BANDOS.ordinal()]); // bando
		player.getVarsManager().sendVarBit(3938, player.getFacade().getGwdKillcount()[Bosses.SARADOMIN.ordinal()]);// sara
		player.getVarsManager().sendVarBit(3942, player.getFacade().getGwdKillcount()[Bosses.ZAMORAK.ordinal()]);// zamy
	}

}
