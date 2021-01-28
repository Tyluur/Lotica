package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/29/2015
 */
public class Donate extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "store", "donate" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getPackets().sendOpenURL("http://www.lotica.soulplayps.com/services/store/");
	}
}
