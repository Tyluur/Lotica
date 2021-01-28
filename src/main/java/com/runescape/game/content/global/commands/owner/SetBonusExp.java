package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.ConfigurationParser;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class SetBonusExp extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "setbonusxp", "dxpwknd" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		try {
			int hours = Integer.parseInt(cmd[1]);
			long overAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours);
			ConfigurationParser.putProperty(ConfigurationParser.DXP_WEEKEND_START_KEY, System.currentTimeMillis());
			ConfigurationParser.putProperty(ConfigurationParser.DXP_WEEKEND_END_KEY, overAt);
			World.sendWorldMessage("<col=" + ChatColors.RED + ">Double experience has now been enabled for the next " + hours + " hours.", false);
		} catch (Exception e) {
			player.sendMessage("Use as ::" + cmd[0] + " hours");
			e.printStackTrace();
		}
	}
}
