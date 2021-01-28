package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/24/2016
 */
public class FloorMaskData extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "maskinfo";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		System.out.println(World.getMask(player.getPlane(), player.getX(), player.getY()));
	}
}
