package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class CID extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "cid";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		ItemInformationLoader.CACHED_DATA.clear();
	}

}
