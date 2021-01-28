package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class Die extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "die";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.applyHit(new Hit(player, player.getHitpoints()));
	}
}
