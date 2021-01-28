package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/4/2016
 */
public class ResetKDR extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "resetkdr";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		if (player.takeMoney(100_000)) {
			player.setKillCount(0);
			player.setDeathCount(0);
			player.sendMessage("Your kill death ratio has been reset.");
		} else {
			player.sendMessage("You must pay 100K coins to have this reset.");
		}
	}
}
