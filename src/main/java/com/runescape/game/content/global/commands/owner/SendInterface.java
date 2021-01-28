package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class SendInterface extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "inter", "interface" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getInterfaceManager().sendInterface(Integer.parseInt(cmd[1]));
	}

}
