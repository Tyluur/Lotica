package com.runescape.game.content.global.commands.support;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.SerializableFilesManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/30/2015
 */
public class SendHome extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "sendhome", "unnull" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1).replaceAll("_", " ");
		Player target = World.getPlayerByDisplayName(name);
		if (target != null) {
			target.getControllerManager().forceStop();
			target.setNextWorldTile(GameConstants.START_PLAYER_LOCATION);
			target.sendMessage(player.getDisplayName() + " has teleported you home.");
		} else {
			name = name.replaceAll(" ", "_");
			try {
				target = SerializableFilesManager.loadPlayer(name);
				if (target == null) {
					player.sendMessage("No such player: " + name);
					return;
				}
				target.setUsername(name.replaceAll(".p", ""));
				target.getControllerManager().removeController();
				target.setLocation(GameConstants.START_PLAYER_LOCATION);
				SerializableFilesManager.savePlayer(target);
				player.sendMessage("unnulled offline player: " + target.getUsername());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
