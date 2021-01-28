package com.runescape.game.content.global.commands.donator;

import com.runescape.game.content.DonatorZone;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class DonorZone extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "dz";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		DonatorZone.enterDonatorzone(player);
	}
}
