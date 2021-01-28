package com.runescape.game.interaction.dialogues.impl.minigame;

import com.runescape.game.content.global.minigames.clanwars.ClanWars;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.Utils;

/**
 * Handles the clan wars viewing dialogue.
 * @author Emperor
 *
 */
public final class ClanWarsViewing extends Dialogue {

	@Override
	public void start() {
		//TITLE: "Your clan does not appear to be in a war."
		sendOptionsDialogue("Select an option", "I want to watch a friend's clan war.", "Show me a battle - any battle.", "Oh, forget it.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		switch (componentId) {
		case OPTION_1:
			player.getAttributes().put("view_name", true);
			player.getPackets().sendRunScript(109, "Enter friend's name:");
			break;
		case OPTION_2:
			if (ClanWars.getCurrentwars().isEmpty()) {
				player.getPackets().sendGameMessage("There are no clan wars going on currently.");
				return;
			}
			player.getAttributes().put("view_clan", ClanWars.getCurrentwars().get(Utils.random(ClanWars.getCurrentwars().size())));
			ClanWars.enter(player);
			break;
		case OPTION_3:
			break;
		}
	}

	@Override
	public void finish() { }

}