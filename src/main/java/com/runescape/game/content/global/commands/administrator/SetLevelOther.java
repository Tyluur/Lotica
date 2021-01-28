package com.runescape.game.content.global.commands.administrator;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.world.player.PlayerSaving;

public class SetLevelOther extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "setlevelo" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		try {
			String targetName = cmd[1];
			int skill = Integer.parseInt(cmd[2]);
			int level = Integer.parseInt(cmd[3]);
			Player target = World.getPlayer(targetName);
			boolean offline = false;
			if (target == null) {
				target = GameScript.getPlayer(Utils.formatPlayerNameForProtocol(targetName));
				offline = true;
			}
			if (target == null) {
				player.sendMessage("No such player!");
				return;
			}
			target.getSkills().setLevel(skill, level, !offline);
			target.getSkills().setXp(skill, Skills.getXPForLevel(level), !offline);
			if (!offline) {
				target.getAppearence().generateAppearenceData();
			}
			player.getPackets().sendGameMessage("Done!");
			if (offline) {
				PlayerSaving.savePlayer(target);
			}
		} catch (Exception e) {
			player.getPackets().sendGameMessage("Use as ::setlevelo name skill level");
			e.printStackTrace();
		}
	}

}
