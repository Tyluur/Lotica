package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;
import com.runescape.utility.world.player.PlayerSaving;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 18, 2015
 */
public class UnmacBan extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "unipban";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1);
		Player target = PlayerSaving.fromFile(Utils.formatPlayerNameForProtocol(name));
		if (target == null) {
			player.sendMessage("No such player by that name, please retry.", false);
			return;
		}
		if (GsonStartup.getClass(PunishmentLoader.class).removePunishment(target.getMacAddress(), PunishmentType.MACBAN)) {
			player.sendMessage("Successfully unmacbanned " + name + "!", false);
		} else {
			player.sendMessage("Couldn't find any ipban to remove for " + name + ", try again.", false);
		}
	}

}
