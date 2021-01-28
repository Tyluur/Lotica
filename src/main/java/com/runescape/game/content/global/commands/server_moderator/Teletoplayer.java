package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/27/2015
 */
public class Teletoplayer extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "teleto";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		Player target = World.getPlayerByDisplayName(getCompleted(cmd, 1));
		if (target == null) {
			player.sendMessage("Could not find user... Try again.", true);
			return;
		}
		player.setNextWorldTile(target);
		player.sendMessage("You teleport to " + target.getUsername() + ".");
	}
}
