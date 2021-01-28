package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Reset extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "reset";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
			int level = (i != 3 ? 1 : 10);
			player.getSkills().setLevel(i, level);
			player.getSkills().setXp(i, Skills.getXPForLevel(level));
		}
	}

}
