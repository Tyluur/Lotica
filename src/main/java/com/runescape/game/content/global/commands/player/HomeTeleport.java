package com.runescape.game.content.global.commands.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class HomeTeleport extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "home";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You can't teleport home until 10 seconds after the end of combat.");
			return;
		}
		Magic.sendNormalTeleportSpell(player, 1, 0, GameConstants.START_PLAYER_LOCATION);
	}
}
