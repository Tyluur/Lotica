package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Player;

import java.text.MessageFormat;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/8/2015
 */
public class KDR extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "kdr";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.setNextForceTalk(new ForceTalk(MessageFormat.format("KDR INFO: [kills={0}, deaths={1}, ratio:{2}]", player.getKillCount(), player.getDeathCount(), ((double) player.getKillCount() / (double) player.getDeathCount()))));
	}
}
