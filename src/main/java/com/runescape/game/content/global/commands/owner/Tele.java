package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Tele extends CommandSkeleton<String[]> {

    @Override
    public String[] getIdentifiers() {
        return new String[] { "tele" };
    }

    @Override
    public boolean consoleCommand() {
        return true;
    }

	@Override
	public boolean shownOnInterface() {
		return false;
	}

	@Override
    public void handleCommand(Player player, String[] cmd) {
        cmd = cmd[1].split(",");
        int plane = Integer.valueOf(cmd[0]);
        int x = Integer.valueOf(cmd[1]) << 6 | Integer.valueOf(cmd[3]);
        int y = Integer.valueOf(cmd[2]) << 6 | Integer.valueOf(cmd[4]);
        player.setNextWorldTile(new WorldTile(x, y, plane));
    }

}