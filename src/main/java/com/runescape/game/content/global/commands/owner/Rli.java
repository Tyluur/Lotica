package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.ItemProperties;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/21/2015
 */
public class Rli extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "rli";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		ItemInformationLoader.CACHED_DATA.clear();
		ItemProperties.loadProperties();
		player.sendMessage("Reloaded all item properties.");
	}
}
