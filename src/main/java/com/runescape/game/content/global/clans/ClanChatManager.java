package com.runescape.game.content.global.clans;

import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * 
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 12, 2013
 */
public class ClanChatManager {
	
	/**
	 * Creates a clan for the player 
	 * @param player The player making the clan
	 * @param name The name of th eclan
	 */
	public static void createClan(Player player, String name) {
		name = Utils.formatPlayerNameForDisplay(name);
		Clan clan = new Clan(name, player);
		ClanLoader.addClanAndSave(clan);
	}
	
	public static void joinMyClan(Player player) {
		if (ClanLoader.getClanByName(player.getDisplayName()) == null) {
			player.getDialogueManager().startDialogue("ClanCreateDialogue");
			return;
		}
	}

}
