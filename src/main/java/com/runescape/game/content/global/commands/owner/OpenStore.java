package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/6/2015
 */
public class OpenStore extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "store";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, getCompleted(cmd, 1)));
	}
}
