package com.runescape.utility.applications.console.listener.responses;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.applications.console.listener.ConsoleResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public class StaffOnlineResponse implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "staff_online";
	}

	@Override
	public void onCall(String text) {
		List<String> staffOnline = new ArrayList<>();
		for (Player player : World.getPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.isStaff()) {
				staffOnline.add(player.getUsername());
			}
		}
		StringBuilder bldr = new StringBuilder();
		System.out.println("There are currently " + staffOnline.size() + " players online.");
		for (int i = 0; i < staffOnline.size(); i++) {
			bldr.append(staffOnline.get(i)).append(i == staffOnline.size() - 1 ? "" : ", ");
		}
		System.out.println("List of staff:\t" + bldr.toString());
	}
}
