package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class SpawnObject extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "obj" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int type = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 10;
		int rotation = cmd.length == 4 ? Integer.parseInt(cmd[3]) : 0;
		if (type > 22 || type < 0) {
			type = 10;
		}
		WorldObject object = new WorldObject(Integer.valueOf(cmd[1]), type, rotation, player.getX(), player.getY(), player.getPlane());
		World.spawnObject(object);
		player.sendMessage("Spawned Object: " + object + "", true);
	}

}
