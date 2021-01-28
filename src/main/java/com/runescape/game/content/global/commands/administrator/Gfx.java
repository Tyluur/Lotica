package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Gfx extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "gfx";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.setNextGraphics(new Graphics(Integer.parseInt(cmd[1])));
	}

}
