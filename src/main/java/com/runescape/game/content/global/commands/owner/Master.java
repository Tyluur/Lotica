package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Master extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "master" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (int skill = 0; skill < 25; skill++) {
			player.getSkills().setXp(skill, 14_000_000);
			player.getSkills().setLevel(skill, 99);
		}
		player.getSkills().restoreSkills();
	}

}
