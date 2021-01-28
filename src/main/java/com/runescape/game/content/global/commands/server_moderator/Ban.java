package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Ban extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "ban";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1);
		Player target = World.getPlayerByDisplayName(name);
		boolean offline = false;
		if (target == null) {
			try {
				target = GameScript.getPlayer(Utils.formatPlayerNameForProtocol(name));
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			offline = true;
			if (target == null) {
				player.sendMessage("Could not find user... Try again.", true);
				return;
			}
		}
		final boolean offlinePlayer = offline;
		final Player targetPlayer = target;
		player.getPackets().requestClientInput(new InputEvent("Enter amount of hours", InputEventType.INTEGER) {
			
			@Override
			public void handleInput() {
				GsonStartup.getOptional(PunishmentLoader.class).ifPresent(c -> {
					c.addPunishment(targetPlayer.getUsername(), PunishmentType.BAN, TimeUnit.HOURS.toMillis(((Integer) getInput()).longValue()));
					if (!offlinePlayer) {
						targetPlayer.forceLogout();
					}
					player.sendMessage("Successfully banned " + targetPlayer.getUsername() + "!", false);
				});
			}
		});
	}

}
