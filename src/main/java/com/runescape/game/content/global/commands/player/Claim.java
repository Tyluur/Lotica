package com.runescape.game.content.global.commands.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/23/2015
 */
public class Claim extends CommandSkeleton<String[]> {
	@Override
	public String[] getIdentifiers() {
		return new String[] { "claim", "checkdonation" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		Long lastTime = player.getAttribute("last_payment_check");
		if (GameConstants.DEBUG || lastTime == null || (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTime) > 10)) {
			DatabaseFunctions.claimGoldPoints(player);
//			DatabaseFunctions.claimSecondaryGoldPoints(player);
			player.putAttribute("last_payment_check", System.currentTimeMillis());
		} else {
			player.sendMessage("You can only use this command once every 10 seconds...");
		}
	}
}
