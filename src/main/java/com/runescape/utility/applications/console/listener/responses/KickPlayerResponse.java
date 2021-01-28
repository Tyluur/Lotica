package com.runescape.utility.applications.console.listener.responses;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.applications.console.listener.ConsoleResponse;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public class KickPlayerResponse implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "kick_user";
	}

	@Override
	public void onCall(String text) {
		Player player = World.getPlayer(text);
		if (player == null) {
			System.out.println("No such player as:\t" + text);
			return;
		}
		player.forceLogout();
		player.getSession().getChannel().close();
		System.out.println("Successfully booted user:\t" + player);
	}
}
