package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class TeleCoordinates extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "xtele" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int x = Integer.parseInt(cmd[1]);
		int y = Integer.parseInt(cmd[2]);
		int z = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 0;
		player.setNextWorldTile(new WorldTile(x, y, z));
	}

}
