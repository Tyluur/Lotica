package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Rla extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "rla";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		AchievementHandler.loadAll(true);
	}

}
