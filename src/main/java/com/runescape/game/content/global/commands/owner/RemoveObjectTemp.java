package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class RemoveObjectTemp extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "rmot";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int objectId = Integer.parseInt(cmd[1]);
		WorldObject object = World.getObjectWithId(player, objectId);
		if (object != null) {
			World.removeObject(object);
			System.out.println(object);
		}
	}

}
