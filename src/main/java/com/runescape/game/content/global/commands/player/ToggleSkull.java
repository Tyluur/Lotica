package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/3/2016
 */
public class ToggleSkull extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "skull" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.setWildernessSkull();
	}
}
