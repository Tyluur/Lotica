package com.runescape.game.content.global.commands.administrator;

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
 * @since May 18, 2015
 */
public class MacBan extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "ipban";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1);
		Player target = World.getPlayerByDisplayName(name);
		if (target == null) {
			try {
				target = GameScript.getPlayer(Utils.formatPlayerNameForProtocol(name));
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (target == null) {
				player.sendMessage("Could not find user... Try again.", true);
				return;
			}
		}
		final Player targetPlayer = target;
		player.getPackets().requestClientInput(new InputEvent("Enter amount of hours", InputEventType.INTEGER) {

			@Override
			public void handleInput() {
				GsonStartup.getOptional(PunishmentLoader.class).ifPresent(c -> {
					c.addPunishment(targetPlayer.getMacAddress(), PunishmentType.MACBAN, TimeUnit.HOURS.toMillis(((Integer) getInput()).longValue()));
					World.getPlayers().forEach(p -> {
						if (p.getMacAddress().equalsIgnoreCase(targetPlayer.getMacAddress())) {
							p.forceLogout();
						}
					});
					player.sendMessage("Successfully ipbanned " + targetPlayer.getUsername() + "!", false);
				});
			}
		});
	}

}
