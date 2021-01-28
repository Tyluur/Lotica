package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/17/2016
 */
public class OwnerBankC extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "bank";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getBank().openBank();
	}
}
