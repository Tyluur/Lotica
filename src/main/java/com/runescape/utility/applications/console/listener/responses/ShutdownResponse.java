package com.runescape.utility.applications.console.listener.responses;

import com.runescape.game.world.World;
import com.runescape.utility.applications.console.listener.ConsoleResponse;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public class ShutdownResponse implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "shutdown";
	}

	@Override
	public void onCall(String text) {
		System.out.println("Calling..");
		World.safeShutdown(0);
		System.out.println("We have called.");
	}
}
