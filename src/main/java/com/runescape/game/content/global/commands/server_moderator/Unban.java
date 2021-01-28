package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 18, 2015
 */
public class Unban extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "unban";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = Utils.formatPlayerNameForProtocol(getCompleted(cmd, 1));
		if (GsonStartup.getClass(PunishmentLoader.class).removePunishment(name, PunishmentType.BAN)) {
			player.sendMessage("Successfully unbanned " + name + ".", true);
		} else {
			player.sendMessage("Could not unban " + name + ", recheck the name.", true);
		}
	}

}
