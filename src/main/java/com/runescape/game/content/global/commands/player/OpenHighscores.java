package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/10/2016
 */
public class OpenHighscores extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "scores";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getPackets().sendOpenURL("http://www.lotica.soulplayps.com/services/highscores/?skill=Overall&type=0");
	}
}
