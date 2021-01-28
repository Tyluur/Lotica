package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class ListCrowns extends CommandSkeleton<String[]>{

	@Override
	public String[] getIdentifiers() {
		return new String[] { "lc" , "listcrowns" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		StringBuilder bldr = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			bldr.append("<img=").append(i).append(">(").append(i).append(")");
		}
		player.sendMessage(bldr.toString(), true);
		bldr = new StringBuilder();
		for (int i = 6; i < 12; i++) {
			bldr.append("<img=").append(i).append(">(").append(i).append(")");
		}
		player.sendMessage(bldr.toString(), true);
		bldr = new StringBuilder();
		for (int i = 12; i < 20; i++) {
			bldr.append("<img=").append(i).append(">(").append(i).append(")");
		}
		player.sendMessage(bldr.toString(), true);
	}

}
