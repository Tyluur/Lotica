package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 18, 2015
 */
public class UnmacMute extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "unipmute";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		Player target = World.getPlayerByDisplayName(getCompleted(cmd, 1));
		if (target == null) {
			player.sendMessage("Could not find user... Try again.", true);
			return;
		}
		String name = getCompleted(cmd, 1);
		if (GsonStartup.getClass(PunishmentLoader.class).removePunishment(target.getMacAddress(), PunishmentType.MACMUTE)) {
			player.sendMessage("Successfully unipmuted " + name + ".", true);
		} else {
			player.sendMessage("Could not unipmute " + name + ", recheck the name.", true);
		}
	}

}
