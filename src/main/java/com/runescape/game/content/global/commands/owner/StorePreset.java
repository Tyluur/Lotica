package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.DefaultPresetsLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/30/2015
 */
public class StorePreset extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "storepreset";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		GsonStartup.getOptional(DefaultPresetsLoader.class).ifPresent(loader -> loader.storePreset(player, getCompleted(cmd, 1)));
	}
}
