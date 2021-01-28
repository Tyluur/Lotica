package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class AddSkillExp extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "addexp";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getSkills().addXp(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
	}
}
