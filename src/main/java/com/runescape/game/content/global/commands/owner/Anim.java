package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Anim extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "anim";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.setNextAnimation(new Animation(Integer.parseInt(cmd[1])));
	}

}
