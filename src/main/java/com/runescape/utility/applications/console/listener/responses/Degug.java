package com.runescape.utility.applications.console.listener.responses;

import com.runescape.utility.applications.console.listener.ConsoleResponse;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/1/2016
 */
public class Degug implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "debug";
	}

	@Override
	public void onCall(String text) {
		System.out.println(CoresManager.DATABASE_WORKER);
		System.out.println(CoresManager.SLOW_EXECUTOR);
		System.out.println(CoresManager.SERVICE);
	}
}
