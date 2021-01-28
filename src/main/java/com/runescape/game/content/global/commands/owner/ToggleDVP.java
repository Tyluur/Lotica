package com.runescape.game.content.global.commands.owner;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.utility.ChatColors;
import com.runescape.utility.ConfigurationParser;

import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/31/2015
 */
public class ToggleDVP extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "toggledvp", "toggledoublevotes"};
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		boolean dvp;
		try {
			ConfigurationParser.putProperty(ConfigurationParser.DVP_WEEKEND_KEY, String.valueOf(dvp = GameConstants.DOUBLE_VOTES_ENABLED = !GameConstants.DOUBLE_VOTES_ENABLED));
			World.sendWorldMessage("<col=" + ChatColors.RED + "><img=" + RightManager.ADMINISTRATOR.getCrownIcon() + ">Double vote points has just been " + (dvp ? "en" : "dis") + "abled!", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
