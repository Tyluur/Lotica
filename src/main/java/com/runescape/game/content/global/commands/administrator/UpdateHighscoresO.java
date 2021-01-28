package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.PlayerSaving;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 9/17/2016
 */
public class UpdateHighscoresO extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "updatehighscoreso";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1);
		Player target = World.getPlayerByDisplayName(name);
		if (target == null) {
			target = PlayerSaving.fromFile(Utils.formatPlayerNameForProtocol(name));
		}
		if (target == null) {
			player.sendMessage("No such player!");
			return;
		}
		DatabaseFunctions.saveHighscores(target);
		player.sendMessage(name + "'s highscores have been updated!");
	}
}
