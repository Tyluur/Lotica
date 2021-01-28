package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/30/2016
 */
public class Email extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "email", "setemail"};
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getPackets().sendOpenURL("http://www.soulplayps.com/forums/index.php?account/contact-details");
	}
}
