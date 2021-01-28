package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Mar 21, 2014
 */
public class SetLevel extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "setlevel", "lvl" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		if (cmd.length < 3) {
			player.getPackets().sendGameMessage("Usage ::setlevel skillId level");
			return;
		}
		try {
			int skill = Integer.parseInt(cmd[1]);
			int level = Integer.parseInt(cmd[2]);
			if (level < 0 || (level > 99 && skill != 24) || level > 120) {
				player.getPackets().sendGameMessage("Please choose a valid level.");
				return;
			}
			player.getSkills().setLevel(skill, level);
			player.getSkills().setXp(skill, Skills.getXPForLevel(level));
			player.getAppearence().generateAppearenceData();
		} catch (NumberFormatException e) {
			player.getPackets().sendGameMessage("Usage ::setlevel skillId level");
		}
	}
}
