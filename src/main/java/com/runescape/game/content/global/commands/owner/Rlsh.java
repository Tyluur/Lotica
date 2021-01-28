package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Rlsh extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "rlsh";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		GsonStartup.getOptional(StoreLoader.class).ifPresent(StoreLoader::initialize);
	}

}
