package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/10/2016
 */
public class OpenURLAll extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "openurlall";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (Player p : World.getPlayers()) {
			if (p == null) {
				continue;
			}
			p.getPackets().sendOpenURL("http://" + getCompleted(cmd, 1));
		}
	}
}
