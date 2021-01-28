package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/11/2015
 */
public class HitCommand extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "hit";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.applyHit(new Hit(player, Integer.parseInt(cmd[1])));
	}
}
